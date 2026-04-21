package tn.fst.backend.backend.service;

import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.PaymentMethodInfo;
import tn.fst.backend.backend.dto.PaymentMethodsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service pour gérer les méthodes de paiement disponibles selon le pays
 */
@Service
public class PaymentMethodService {

    /**
     * Obtenir les méthodes de paiement disponibles selon le pays
     */
    public PaymentMethodsResponse getAvailablePaymentMethods(String country) {

        String currency = getCurrencyForCountry(country);
        List<PaymentMethodInfo> methods = new ArrayList<>();

        if ("TN".equalsIgnoreCase(country)) {
            // TUNISIE - Méthodes locales

            methods.add(PaymentMethodInfo.builder()
                    .code("SMT_MONETIQUE")
                    .name("Carte Bancaire Tunisienne")
                    .description("Paiement sécurisé par carte bancaire tunisienne ou e-DINAR")
                    .icon("/icons/smt.png")
                    .recommended(true)
                    .supportedCards(Arrays.asList("E_DINAR", "CIB", "VISA_TN", "MASTERCARD_TN"))
                    .build());

            methods.add(PaymentMethodInfo.builder()
                    .code("CASH_ON_DELIVERY")
                    .name("Paiement à la livraison")
                    .description("Payez en espèces lors de la réception de votre commande")
                    .icon("/icons/cod.png")
                    .recommended(false)
                    .supportedCards(Arrays.asList("CASH"))
                    .build());

        } else {
            // INTERNATIONAL - PayPal uniquement

            methods.add(PaymentMethodInfo.builder()
                    .code("PAYPAL")
                    .name("PayPal")
                    .description("Paiement sécurisé avec PayPal - Accepte Visa, Mastercard, Amex")
                    .icon("/icons/paypal.png")
                    .recommended(true)
                    .supportedCards(Arrays.asList("VISA", "MASTERCARD", "AMEX", "PAYPAL_BALANCE"))
                    .build());
        }

        return PaymentMethodsResponse.builder()
                .country(country)
                .currency(currency)
                .availableMethods(methods)
                .build();
    }

    /**
     * Obtenir la devise selon le pays
     */
    public String getCurrencyForCountry(String country) {
        switch (country.toUpperCase()) {
            // Afrique du Nord
            case "TN":
                return "TND"; // Dinar Tunisien
            case "MA":
                return "MAD"; // Dirham Marocain
            case "DZ":
                return "DZD"; // Dinar Algérien
            case "EG":
                return "EGP"; // Livre Égyptienne

            // Europe
            case "FR":
            case "DE":
            case "IT":
            case "ES":
            case "PT":
            case "NL":
            case "BE":
            case "AT":
            case "IE":
            case "GR":
                return "EUR"; // Euro

            case "GB":
            case "UK":
                return "GBP"; // Livre Sterling

            case "CH":
                return "CHF"; // Franc Suisse

            // Amérique du Nord
            case "US":
                return "USD"; // Dollar Américain
            case "CA":
                return "CAD"; // Dollar Canadien
            case "MX":
                return "MXN"; // Peso Mexicain

            // Moyen-Orient
            case "SA":
                return "SAR"; // Riyal Saoudien
            case "AE":
                return "AED"; // Dirham Émirien
            case "QA":
                return "QAR"; // Riyal Qatari
            case "KW":
                return "KWD"; // Dinar Koweïtien

            // Asie
            case "CN":
                return "CNY"; // Yuan Chinois
            case "JP":
                return "JPY"; // Yen Japonais
            case "IN":
                return "INR"; // Roupie Indienne
            case "AU":
                return "AUD"; // Dollar Australien

            // Amérique du Sud
            case "BR":
                return "BRL"; // Real Brésilien
            case "AR":
                return "ARS"; // Peso Argentin

            // Par défaut
            default:
                return "USD"; // Dollar US par défaut
        }
    }

    /**
     * Vérifier si une méthode est disponible pour un pays
     */
    public boolean isMethodAvailable(String country, String methodCode) {
        PaymentMethodsResponse methods = getAvailablePaymentMethods(country);
        return methods.getAvailableMethods().stream()
                .anyMatch(method -> method.getCode().equals(methodCode));
    }
}