package com.siec_acc.repository;

import com.siec_acc.entity.ActivityLogEntity;
import com.siec_acc.entity.ApprovalRequestEntity;
import com.siec_acc.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRoleKey(String roleKey);
    boolean existsByRoleKey(String roleKey);

    @Query("select r from RoleEntity r left join fetch r.permissions where upper(r.roleKey) = upper(:roleKey)")
    Optional<RoleEntity> findWithPermissionsByRoleKey(@Param("roleKey") String roleKey);
}

