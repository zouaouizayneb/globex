package tn.fst.backend.backend.entity;

public enum Permission {
    // User permissions
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,

    // Order permissions
    ORDER_READ,
    ORDER_CREATE,
    ORDER_UPDATE,
    ORDER_DELETE,
    ORDER_APPROVE,

    // Product permissions
    PRODUCT_READ,
    PRODUCT_CREATE,
    PRODUCT_UPDATE,
    PRODUCT_DELETE,

    // Stock permissions
    STOCK_READ,
    STOCK_UPDATE,

    // Invoice permissions
    INVOICE_READ,
    INVOICE_CREATE,
    INVOICE_UPDATE,
    INVOICE_DELETE,

    // Payment permissions
    PAYMENT_READ,
    PAYMENT_CREATE,
    PAYMENT_UPDATE,

    // Report permissions
    REPORT_VIEW,
    REPORT_EXPORT,

    // Client permissions
    CLIENT_READ,
    CLIENT_CREATE,
    CLIENT_UPDATE,
    CLIENT_DELETE
}