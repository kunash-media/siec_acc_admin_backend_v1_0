package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.CustomerPaymentRequestDTO;
import com.siec_acc.dto.response.CustomerPaymentResponseDTO;
import com.siec_acc.dto.response.CustomerPaymentSummaryDTO;
import com.siec_acc.dto.response.OrganizationSummaryDTO;
import com.siec_acc.entity.ClientEntity;
import com.siec_acc.entity.CustomerPaymentEntity;
import com.siec_acc.entity.OrganizationEntity;
import com.siec_acc.entity.InvoiceEntity;
import com.siec_acc.enum_status.InvoiceStatus;
import com.siec_acc.repository.InvoiceRepository;
import com.siec_acc.exceptions.DuplicateResourceException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.ClientRepository;
import com.siec_acc.repository.CustomerPaymentRepository;
import com.siec_acc.repository.OrganizationRepository;
import com.siec_acc.service.CustomerPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerPaymentServiceImpl implements CustomerPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerPaymentServiceImpl.class);

    private final CustomerPaymentRepository customerPaymentRepository;
    private final OrganizationRepository organizationRepository;
    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public CustomerPaymentServiceImpl(CustomerPaymentRepository customerPaymentRepository,
                                      OrganizationRepository organizationRepository,
                                      ClientRepository clientRepository,
                                      InvoiceRepository invoiceRepository) {
        this.customerPaymentRepository = customerPaymentRepository;
        this.organizationRepository = organizationRepository;
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public CustomerPaymentResponseDTO createPayment(CustomerPaymentRequestDTO request) {
        validateRequest(request, false);
        OrganizationEntity organization = findOrganization(request.getOrganizationId());
        ClientEntity client = findClient(request.getClientStrId());

        String receiptNumber = request.getReceiptNumber() == null ? "" : request.getReceiptNumber().trim();
        if (receiptNumber.isBlank()) receiptNumber = generateReceiptNumber();
        if (customerPaymentRepository.existsByReceiptNumberIgnoreCase(receiptNumber)) {
            throw new DuplicateResourceException("Payment with receipt number '" + receiptNumber + "' already exists.");
        }

        request.setReceiptNumber(receiptNumber);
        CustomerPaymentEntity payment = new CustomerPaymentEntity();
        payment.setPaymentId(generatePaymentId());
        payment.setOrganization(organization);
        applyFields(payment, request, client, false);
        attachInvoice(payment, request.getInvoicePrimeId());
        calculateAmounts(payment);
        CustomerPaymentEntity saved = customerPaymentRepository.save(payment);
        recalculateInvoice(saved.getInvoicePrimeId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerPaymentResponseDTO getPayment(String paymentId) {
        return toResponse(findPayment(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPaymentResponseDTO> getAllPayments() {
        return customerPaymentRepository.findAllByOrderByPaymentDateDescPaymentPrimeIdDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPaymentResponseDTO> getPaymentsByOrganization(String organizationId) {
        findOrganization(organizationId);
        return customerPaymentRepository
                .findAllByOrganization_OrganizationIdOrderByPaymentDateDescPaymentPrimeIdDesc(organizationId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPaymentResponseDTO> getPaymentsByCustomer(String organizationId, String clientStrId) {
        findOrganization(organizationId);
        findClient(clientStrId);
        return customerPaymentRepository
                .findAllByOrganization_OrganizationIdAndClientStrIdOrderByPaymentDateDescPaymentPrimeIdDesc(organizationId, clientStrId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPaymentSummaryDTO> getCustomerPaymentSummary(String organizationId) {
        findOrganization(organizationId);
        List<CustomerPaymentEntity> payments = customerPaymentRepository
                .findAllByOrganization_OrganizationIdOrderByPaymentDateDescPaymentPrimeIdDesc(organizationId);

        Map<String, CustomerPaymentSummaryDTO> summaryMap = new LinkedHashMap<>();

        for (CustomerPaymentEntity payment : payments) {
            String clientStrId = payment.getClientStrId();
            if (clientStrId == null || clientStrId.isBlank()) continue;

            ClientEntity client = clientRepository.findByClientStrId(clientStrId).orElse(null);
            if (client == null) continue;

            CustomerPaymentSummaryDTO summary = summaryMap.computeIfAbsent(clientStrId, key -> {
                CustomerPaymentSummaryDTO dto = new CustomerPaymentSummaryDTO();
                dto.setClientPrimeId(client.getClientPrimeId());
                dto.setClientStrId(client.getClientStrId());
                dto.setCustomerName(client.getClientName());
                dto.setCustomerCompanyName(client.getClientCompanyName());
                dto.setTotalTransactions(0);
                dto.setTotalPayment(BigDecimal.ZERO);
                dto.setTotalAllocated(BigDecimal.ZERO);
                dto.setOutstanding(BigDecimal.ZERO);
                return dto;
            });

            summary.setTotalTransactions(summary.getTotalTransactions() + 1);
            summary.setTotalPayment(summary.getTotalPayment().add(zero(payment.getAmount())));
            summary.setTotalAllocated(summary.getTotalAllocated().add(zero(payment.getAllocatedAmount())));
            summary.setOutstanding(summary.getOutstanding().add(calculateOutstanding(payment)));
            if (summary.getLastPaymentDate() == null ||
                    (payment.getPaymentDate() != null && payment.getPaymentDate().isAfter(summary.getLastPaymentDate()))) {
                summary.setLastPaymentDate(payment.getPaymentDate());
            }
        }

        return new ArrayList<>(summaryMap.values());
    }

    @Override
    @Transactional
    public CustomerPaymentResponseDTO updatePayment(String paymentId, CustomerPaymentRequestDTO request) {
        validateRequest(request, false);
        CustomerPaymentEntity payment = findPayment(paymentId);
        checkReceiptDuplicate(payment, request.getReceiptNumber());
        ClientEntity client = findClient(request.getClientStrId());
        Long previousInvoiceId = payment.getInvoicePrimeId();
        payment.setOrganization(findOrganization(request.getOrganizationId()));
        applyFields(payment, request, client, false);
        attachInvoice(payment, request.getInvoicePrimeId());
        calculateAmounts(payment);
        CustomerPaymentEntity saved = customerPaymentRepository.save(payment);
        recalculateInvoice(previousInvoiceId);
        recalculateInvoice(saved.getInvoicePrimeId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerPaymentResponseDTO patchPayment(String paymentId, CustomerPaymentRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Payment request is required.");
        CustomerPaymentEntity payment = findPayment(paymentId);
        Long previousInvoiceId = payment.getInvoicePrimeId();

        if (request.getOrganizationId() != null) payment.setOrganization(findOrganization(request.getOrganizationId()));
        if (request.getReceiptNumber() != null && !request.getReceiptNumber().isBlank()) {
            checkReceiptDuplicate(payment, request.getReceiptNumber());
        }
        if (request.getClientStrId() != null && !request.getClientStrId().isBlank()) {
            applyClient(payment, findClient(request.getClientStrId()));
        }
        applyFields(payment, request, null, true);
        if (request.getInvoicePrimeId() != null) attachInvoice(payment, request.getInvoicePrimeId());
        validateSavedPayment(payment);
        calculateAmounts(payment);
        CustomerPaymentEntity saved = customerPaymentRepository.save(payment);
        recalculateInvoice(previousInvoiceId);
        recalculateInvoice(saved.getInvoicePrimeId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePayment(String paymentId) {
        CustomerPaymentEntity payment = findPayment(paymentId);
        Long invoicePrimeId = payment.getInvoicePrimeId();
        customerPaymentRepository.delete(payment);
        recalculateInvoice(invoicePrimeId);
        logger.info("Customer payment deleted: {}", paymentId);
    }

    private CustomerPaymentEntity findPayment(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Payment ID is required.");
        return customerPaymentRepository.findByPaymentId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer payment not found: " + id));
    }

    private OrganizationEntity findOrganization(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Organization ID is required.");
        return organizationRepository.findByOrganizationId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
    }

    private ClientEntity findClient(String clientStrId) {
        if (clientStrId == null || clientStrId.isBlank()) {
            throw new IllegalArgumentException("Customer is required.");
        }
        return clientRepository.findByClientStrId(clientStrId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + clientStrId));
    }

    private void validateRequest(CustomerPaymentRequestDTO request, boolean partial) {
        if (request == null) throw new IllegalArgumentException("Payment request is required.");
        if (partial) return;
        if (request.getOrganizationId() == null || request.getOrganizationId().isBlank()) {
            throw new IllegalArgumentException("Organization ID is required.");
        }
        if (request.getClientStrId() == null || request.getClientStrId().isBlank()) {
            throw new IllegalArgumentException("Customer is required.");
        }
        if (request.getPaymentDate() == null) throw new IllegalArgumentException("Payment date is required.");
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (request.getPaymentMode() == null || request.getPaymentMode().isBlank()) {
            throw new IllegalArgumentException("Payment mode is required.");
        }
    }

    private void validateSavedPayment(CustomerPaymentEntity payment) {
        if (payment.getOrganization() == null) throw new IllegalArgumentException("Organization is required.");
        if (payment.getClientStrId() == null || payment.getClientStrId().isBlank()) throw new IllegalArgumentException("Customer is required.");
        if (payment.getReceiptNumber() == null || payment.getReceiptNumber().isBlank()) throw new IllegalArgumentException("Receipt number is required.");
        if (payment.getPaymentDate() == null) throw new IllegalArgumentException("Payment date is required.");
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        if (payment.getPaymentMode() == null || payment.getPaymentMode().isBlank()) throw new IllegalArgumentException("Payment mode is required.");
    }

    private void applyFields(CustomerPaymentEntity payment, CustomerPaymentRequestDTO request, ClientEntity client, boolean partial) {
        if (!partial || request.getReceiptNumber() != null) payment.setReceiptNumber(trim(request.getReceiptNumber()));
        if (!partial || request.getPaymentDate() != null) payment.setPaymentDate(request.getPaymentDate());
        if (!partial || request.getAmount() != null) payment.setAmount(request.getAmount());
        if (!partial || request.getPaymentMode() != null) payment.setPaymentMode(trim(request.getPaymentMode()));
        if (!partial || request.getReferenceNumber() != null) payment.setReferenceNumber(trim(request.getReferenceNumber()));
        if (!partial || request.getBankAccount() != null) payment.setBankAccount(trim(request.getBankAccount()));
        if (!partial || request.getInvoiceReference() != null) payment.setInvoiceReference(trim(request.getInvoiceReference()));
        if (!partial || request.getInvoicePrimeId() != null) payment.setInvoicePrimeId(request.getInvoicePrimeId());
        if (!partial || request.getAllocatedAmount() != null) payment.setAllocatedAmount(request.getAllocatedAmount());
        if (!partial || request.getStatus() != null) payment.setStatus(trim(request.getStatus()));
        if (!partial || request.getNotes() != null) payment.setNotes(trim(request.getNotes()));
        if (client != null) applyClient(payment, client);
    }

    private void applyClient(CustomerPaymentEntity payment, ClientEntity client) {
        payment.setClientStrId(client.getClientStrId());
    }

    private void attachInvoice(CustomerPaymentEntity payment, Long invoicePrimeId) {
        if (invoicePrimeId == null) {
            payment.setInvoicePrimeId(null);
            return;
        }
        InvoiceEntity invoice = invoiceRepository.findById(invoicePrimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoicePrimeId));
        if (invoice.getInvoiceCustomerId() == null || payment.getClientStrId() == null) {
            throw new IllegalArgumentException("The selected invoice must belong to the selected customer.");
        }
        ClientEntity client = clientRepository.findByClientStrId(payment.getClientStrId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + payment.getClientStrId()));
        if (!invoice.getInvoiceCustomerId().equals(client.getClientPrimeId())) {
            throw new IllegalArgumentException("The selected invoice does not belong to the selected customer.");
        }
        payment.setInvoicePrimeId(invoicePrimeId);
        payment.setInvoiceReference(invoice.getInvoiceStrId());
        payment.setAllocatedAmount(invoice.getInvoiceTotalAmountInr() == null ? invoice.getInvoiceTotalAmount() : invoice.getInvoiceTotalAmountInr());
    }

    private void recalculateInvoice(Long invoicePrimeId) {
        if (invoicePrimeId == null) return;
        InvoiceEntity invoice = invoiceRepository.findById(invoicePrimeId).orElse(null);
        if (invoice == null) return;
        BigDecimal received = customerPaymentRepository.findAllByInvoicePrimeId(invoicePrimeId).stream()
                .filter(p -> p.getStatus() == null || "RECEIVED".equalsIgnoreCase(p.getStatus()))
                .map(p -> zero(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = zero(invoice.getInvoiceTotalAmountInr() == null ? invoice.getInvoiceTotalAmount() : invoice.getInvoiceTotalAmountInr());
        if (received.compareTo(total) > 0) {
            throw new IllegalArgumentException("Total received payments cannot exceed the invoice total.");
        }
        invoice.setInvoicePaidAmount(received);
        if (received.compareTo(BigDecimal.ZERO) <= 0) invoice.setInvoiceStatus(InvoiceStatus.UNPAID);
        else if (received.compareTo(total) >= 0) invoice.setInvoiceStatus(InvoiceStatus.PAID);
        else invoice.setInvoiceStatus(InvoiceStatus.PARTIAL);
        invoiceRepository.save(invoice);
    }

    private void calculateAmounts(CustomerPaymentEntity payment) {
        BigDecimal amount = zero(payment.getAmount());
        BigDecimal allocated = zero(payment.getAllocatedAmount());
        if (allocated.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Invoice amount cannot be negative.");
        if (allocated.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(allocated) > 0) {
            throw new IllegalArgumentException("Payment amount cannot be greater than invoice amount.");
        }
        payment.setOutstandingAmount(allocated.subtract(amount).max(BigDecimal.ZERO));
        payment.setAllocatedAmount(allocated);
        if (payment.getStatus() == null || payment.getStatus().isBlank()) payment.setStatus("RECEIVED");
    }

    private void checkReceiptDuplicate(CustomerPaymentEntity payment, String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) return;
        String requested = receiptNumber.trim();
        if (requested.equalsIgnoreCase(payment.getReceiptNumber())) return;
        if (customerPaymentRepository.existsByReceiptNumberIgnoreCase(requested)) {
            throw new DuplicateResourceException("Payment with receipt number '" + requested + "' already exists.");
        }
    }

    private String generatePaymentId() {
        return "CP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String generateReceiptNumber() {
        String value;
        do {
            value = "RCPT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (customerPaymentRepository.existsByReceiptNumberIgnoreCase(value));
        return value;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calculateOutstanding(CustomerPaymentEntity payment) {
        BigDecimal invoiceAmount = zero(payment.getAllocatedAmount());
        BigDecimal paymentAmount = zero(payment.getAmount());
        return invoiceAmount.subtract(paymentAmount).max(BigDecimal.ZERO);
    }

    private String trim(String value) { return value == null ? null : value.trim(); }

    private CustomerPaymentResponseDTO toResponse(CustomerPaymentEntity payment) {
        CustomerPaymentResponseDTO response = new CustomerPaymentResponseDTO();
        response.setOrganizationId(payment.getOrganization() == null ? null : payment.getOrganization().getOrganizationId());
        response.setOrganization(toOrganizationSummary(payment.getOrganization()));
        response.setPaymentId(payment.getPaymentId());
        response.setReceiptNumber(payment.getReceiptNumber());
        response.setPaymentDate(payment.getPaymentDate());
        response.setClientStrId(payment.getClientStrId());
        response.setAmount(payment.getAmount());
        response.setPaymentMode(payment.getPaymentMode());
        response.setReferenceNumber(payment.getReferenceNumber());
        response.setBankAccount(payment.getBankAccount());
        response.setInvoiceReference(payment.getInvoiceReference());
        response.setInvoicePrimeId(payment.getInvoicePrimeId());
        response.setAllocatedAmount(payment.getAllocatedAmount());
        response.setOutstandingAmount(payment.getOutstandingAmount());
        response.setStatus(payment.getStatus());
        response.setNotes(payment.getNotes());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        if (payment.getClientStrId() != null) {
            clientRepository.findByClientStrId(payment.getClientStrId()).ifPresent(client -> {
                response.setClientPrimeId(client.getClientPrimeId());
                response.setCustomerName(client.getClientName());
                response.setCustomerCompanyName(client.getClientCompanyName());
            });
        }
        return response;
    }

    private OrganizationSummaryDTO toOrganizationSummary(OrganizationEntity organization) {
        if (organization == null) return null;
        OrganizationSummaryDTO response = new OrganizationSummaryDTO();
        response.setOrganizationId(organization.getOrganizationId());
        response.setCompanyName(organization.getCompanyName());
        response.setLegalName(organization.getLegalName());
        response.setLogoUrl(organization.getLogoUrl());
        response.setAddress(organization.getAddress());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        response.setCountry(organization.getCountry());
        response.setPincode(organization.getPincode());
        response.setPhone(organization.getPhone());
        response.setEmail(organization.getEmail());
        response.setWebsite(organization.getWebsite());
        response.setGstRegistered(organization.getGstRegistered());
        response.setGstNumber(organization.getGstNumber());
        response.setBankName(organization.getBankName());
        response.setBankAccountNumber(organization.getBankAccountNumber());
        response.setBankIfscCode(organization.getBankIfscCode());
        return response;
    }
}