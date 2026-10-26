package com.siec_acc.enum_status;
public enum AppModule {

    DASHBOARD("dashboard"),
    ACTIVITY_LOG("activity-log"),
    REPORTS("reports"),

    PROJECTS("projects",
            lookup("/api/projects/v1", "PROJECT")),

    PRODUCTS("products",
            lookup("/api/products/v1", "PRODUCT"),
            lookup("/api/variants/v1", "VARIANT")),

    CLIENTS("clients",
            lookup("/api/clients/v1", "CLIENT")),

    VENDORS("vendors",
            route("/api/vendor", "VENDOR")),

    QUOTATIONS("quotations",
            route("/api/v1/quotes", "QUOTE")),

    SALES_ORDERS("sales-orders",
            route("/api/sales-orders", "SALES_ORDER")),

    DELIVERY_CHALLAN("delivery-challan",
            route("/api/v1/delivery-challans", "DELIVERY_CHALLAN")),

    INVOICES("invoices",
            route("/api/v1/invoices", "INVOICE"),
            route("/api/v1/proformas", "PROFORMA")),

    CUSTOMER_PAYMENTS("customer-payments",
            route("/api/customer-payments", "CUSTOMER_PAYMENT")),

    PURCHASE_ORDERS("purchase-orders",
            route("/api/purchase-orders/v1", "PURCHASE_ORDER"),
            route("/api/purchase-bills/v1", "PURCHASE_BILL"),
            route("/api/purchases/v1", "PURCHASE"),
            route("/api/goods-receipts/v1", "GOODS_RECEIPT")),

    VENDOR_PAYMENTS("vendor-payments",
            route("/api/vendor-payments/v1", "VENDOR_PAYMENT")),

    INVENTORY("inventory",
            route("/api/inventory/v1", "INVENTORY"),
            lookup("/api/warehouses/v1", "WAREHOUSE")),

    EXPENSES("expenses",
            route("/api/expenses", "EXPENSE")),

    USERS("users",
            route("/api/admin", "ADMIN"),
            route("/api/admin/roles", "ROLE")),

    SETTINGS("settings",
            lookup("/api/organization", "ORGANIZATION"));

    /** prefix + entity type + whether GET needs only a login (reference data used by other pages). */
    public record Route(String prefix, String entityType, boolean sharedLookup) {}

    /** Result of resolving a request path. */
    public record Match(AppModule module, String entityType, boolean sharedLookup) {}

    private final String key;
    private final Route[] routes;

    AppModule(String key, Route... routes) {
        this.key = key;
        this.routes = routes;
    }

    public String getKey() { return key; }

    private static Route route(String prefix, String entityType) {
        return new Route(prefix, entityType, false);
    }

    private static Route lookup(String prefix, String entityType) {
        return new Route(prefix, entityType, true);
    }

    /** Longest matching prefix wins; null = path belongs to no module (left untouched). */
    public static Match resolve(String path) {
        Match best = null;
        int bestLen = -1;
        for (AppModule m : values()) {
            for (Route r : m.routes) {
                if (r.prefix().length() > bestLen && matches(path, r.prefix())) {
                    best = new Match(m, r.entityType(), r.sharedLookup());
                    bestLen = r.prefix().length();
                }
            }
        }
        return best;
    }

    // "/api/vendor" matches "/api/vendor" and "/api/vendor/..." but NOT "/api/vendor-payments/..."
    private static boolean matches(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}