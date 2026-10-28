package com.siec_acc.config;

import com.siec_acc.entity.rbac.ApprovalRuleEntity;
import com.siec_acc.repository.ApprovalRuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class ApprovalRuleSeeder implements ApplicationRunner {

    private final ApprovalRuleRepository repository;

    public ApprovalRuleSeeder(ApprovalRuleRepository repository) { this.repository = repository; }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("quotations", "QUOTE", "DELETE", "GATE", null);
        seed("sales-orders", "SALES_ORDER", "DELETE", "GATE", null);
        seed("delivery-challan", "DELIVERY_CHALLAN", "DELETE", "GATE", null);
        seed("invoices", "INVOICE", "DELETE", "GATE", null);
        seed("invoices", "PROFORMA", "DELETE", "GATE", null);
        // Review-flag rules are stored now; they take effect in the next patch
        BigDecimal threshold = new BigDecimal("50000");
        seed("customer-payments", "CUSTOMER_PAYMENT", "CREATE", "REVIEW", threshold);
        seed("vendor-payments", "VENDOR_PAYMENT", "CREATE", "REVIEW", threshold);

    }

    private void seed(String module, String entityType, String action, String mode, BigDecimal min) {
        if (repository.existsByEntityTypeAndAction(entityType, action)) return;
        repository.save(ApprovalRuleEntity.builder()
                .moduleKey(module).entityType(entityType).action(action).mode(mode).minAmount(min).active(true)
                .build());
    }
}
