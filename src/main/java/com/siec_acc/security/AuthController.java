package com.siec_acc.security;

import com.siec_acc.entity.rbac.AdminEntity;
import com.siec_acc.repository.AdminRepository;
import com.siec_acc.service.rbac.ActivityLogService;
import com.siec_acc.service.rbac.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService adminUserDetailsService;
    private final AdminRepository adminRepository;
    private final PermissionService permissionService;
    private final ActivityLogService activityLogService;


    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(@Qualifier("adminAuthenticationManager") AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          AdminUserDetailsService adminUserDetailsService, AdminRepository adminRepository,
                          PermissionService permissionService, ActivityLogService activityLogService) {
        this.authenticationManager   = authenticationManager;
        this.jwtUtil                 = jwtUtil;
        this.adminUserDetailsService = adminUserDetailsService;
        this.adminRepository = adminRepository;
        this.permissionService = permissionService;
        this.activityLogService = activityLogService;
        logger.info("AuthController initialized");
    }

    private void safeFailedLoginLog(HttpServletRequest req, String mobile, String reason) {
        try {
            activityLogService.recordFailedLogin(req, mobile, reason);
        } catch (Exception e) {
            logger.warn("Failed-login log write failed: {}", e.getMessage());
        }
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeMs) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "Strict" : "Lax")
                .path(path)
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "Strict" : "Lax")
                .path(path)
                .maxAge(0)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {

        String mobile   = request.get("mobile");
        String password = request.get("password");

        logger.info("Login attempt for mobile: {}", mobile);

        if (mobile == null || mobile.trim().isEmpty()) {
            logger.warn("Login attempt with missing mobile number");
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(mobile, password)
            );

            final UserDetails userDetails = adminUserDetailsService.loadUserByUsername(mobile);
            final String accessToken  = jwtUtil.generateToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie("admin_token",   accessToken,  "/", accessTokenExpiration).toString());
            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie("refresh_token", refreshToken, "/", refreshTokenExpiration).toString());

            logger.info("Login successful for mobile: {} | Cookies set (secure={})", mobile, cookieSecure);

            AdminDetails adminDetails = (AdminDetails) userDetails;

            try {
                AdminEntity loggedIn = adminDetails.getAdminEntity();
                loggedIn.setAdminLastLoginAt(java.time.LocalDateTime.now());
                adminRepository.save(loggedIn);
                activityLogService.record(adminDetails, httpRequest, "auth", "LOGIN", "ADMIN",
                        adminDetails.getAdminId(), null, "Logged in", 200);
            } catch (Exception e) {
                logger.warn("Post-login bookkeeping failed (login continues): {}", e.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "mobile",  mobile,
                    "adminId", adminDetails.getAdminId() != null ? adminDetails.getAdminId() : "",
                    "role",    adminDetails.getRole()    != null ? adminDetails.getRole()    : ""
            ));

        } catch (BadCredentialsException e) {
            logger.warn("Login failed - invalid credentials for mobile: {}", mobile);
            safeFailedLoginLog(httpRequest, mobile, "Invalid credentials");
            return ResponseEntity.status(401).body("Invalid mobile or password");
        } catch (DisabledException | LockedException e) {
            logger.warn("Login blocked - account disabled or locked for mobile: {}", mobile);
            safeFailedLoginLog(httpRequest, mobile, "Account disabled or locked");
            return ResponseEntity.status(403).body(Map.of(
                    "message", "Your account is disabled or locked. Contact your administrator."));
        } catch (Exception e) {
            logger.error("Unexpected error during login for mobile: {}", mobile, e);
            return ResponseEntity.status(500).body("Authentication error - please try again later");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof AdminDetails adminDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized - Login required"));
        }
        String role = adminDetails.getRole() != null ? adminDetails.getRole() : "";
        AdminEntity a = adminDetails.getAdminEntity();
        String name = ((a.getAdminFirstName() == null ? "" : a.getAdminFirstName()) + " "
                + (a.getAdminLastName() == null ? "" : a.getAdminLastName())).trim();
        return ResponseEntity.ok(Map.of(
                "adminId", adminDetails.getAdminId() != null ? adminDetails.getAdminId() : "",
                "mobile",  adminDetails.getMobileNumber(),
                "role",    role,
                "name",    name,
                "permissions", permissionService.permissionMap(role)
        ));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {

        logger.info("Token refresh request received");

        String refreshToken = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            logger.warn("Refresh attempt with no refresh_token cookie");
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        try {
            String mobile = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = adminUserDetailsService.loadUserByUsername(mobile);

            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                logger.warn("Invalid or expired refresh token for: {}", mobile);
                return ResponseEntity.status(401).body("Refresh token invalid or expired");
            }

            String newAccessToken = jwtUtil.generateToken(userDetails);

            response.addHeader(HttpHeaders.SET_COOKIE,
                    buildCookie("admin_token", newAccessToken, "/", accessTokenExpiration).toString());

            logger.info("Access token refreshed successfully for: {}", mobile);
            return ResponseEntity.ok(Map.of("message", "Token refreshed"));

        } catch (Exception e) {
            logger.error("Error during token refresh", e);
            return ResponseEntity.status(401).body("Token refresh failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        logger.info("Logout request received - clearing auth cookies");

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("admin_token",   "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("refresh_token", "/").toString());

        logger.info("Logout successful - cookies cleared");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
