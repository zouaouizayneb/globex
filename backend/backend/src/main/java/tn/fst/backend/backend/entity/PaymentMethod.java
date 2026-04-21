package tn.fst.backend.backend.entity;

public enum PaymentMethod {
    // Paiements tunisiens
    SMT_MONETIQUE,
    E_DINAR,
    PAYMEE,
    KONNECT,

    // Paiements internationaux
    STRIPE,
    PAYPAL,
    TWO_CHECKOUT,

    // Autres
    CASH_ON_DELIVERY,
    BANK_TRANSFER
}