package tn.fst.backend.backend.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class TaxService {

    private static final Map<String, BigDecimal> COUNTRY_TAX_RATES = new HashMap<>();

    static {
        // Sample tax rates for an international e-commerce
        COUNTRY_TAX_RATES.put("Tunisia", new BigDecimal("19.00")); // VAT 19%
        COUNTRY_TAX_RATES.put("France", new BigDecimal("20.00"));  // TVA 20%
        COUNTRY_TAX_RATES.put("USA", new BigDecimal("7.50"));     // Average Sales Tax
        COUNTRY_TAX_RATES.put("Germany", new BigDecimal("19.00")); // MwSt 19%
        COUNTRY_TAX_RATES.put("Canada", new BigDecimal("13.00"));  // HST 13%
        COUNTRY_TAX_RATES.put("UK", new BigDecimal("20.00"));      // VAT 20%
        COUNTRY_TAX_RATES.put("Default", new BigDecimal("15.00")); // Default fallback
    }

    public BigDecimal getTaxRateForCountry(String country) {
        if (country == null) return COUNTRY_TAX_RATES.get("Default");
        return COUNTRY_TAX_RATES.getOrDefault(country, COUNTRY_TAX_RATES.get("Default"));
    }

    public BigDecimal calculateTax(BigDecimal subtotal, String country) {
        BigDecimal rate = getTaxRateForCountry(country);
        return subtotal.multiply(rate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
    }

    public tn.fst.backend.backend.dto.TaxCalculationResponse calculateTax(tn.fst.backend.backend.dto.TaxCalculationRequest request) {
        BigDecimal rate = getTaxRateForCountry(request.getCountry());
        BigDecimal amount = request.getSubtotal().multiply(rate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        
        String type = "VAT";
        if ("USA".equalsIgnoreCase(request.getCountry())) type = "Sales Tax";
        else if ("Canada".equalsIgnoreCase(request.getCountry())) type = "HST/GST";

        return tn.fst.backend.backend.dto.TaxCalculationResponse.builder()
                .taxAmount(amount)
                .taxRate(rate)
                .taxType(type)
                .build();
    }
}