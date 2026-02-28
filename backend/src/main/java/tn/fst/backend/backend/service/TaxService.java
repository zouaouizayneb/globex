package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.TaxCalculationRequest;
import tn.fst.backend.backend.dto.TaxCalculationResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaxService {

    // Tax rates by country (ISO codes)
    private static final Map<String, TaxInfo> TAX_RATES = new HashMap<>();

    static {
        // Europe (VAT)
        TAX_RATES.put("FR", new TaxInfo(BigDecimal.valueOf(0.20), "VAT")); // France 20%
        TAX_RATES.put("DE", new TaxInfo(BigDecimal.valueOf(0.19), "VAT")); // Germany 19%
        TAX_RATES.put("IT", new TaxInfo(BigDecimal.valueOf(0.22), "VAT")); // Italy 22%
        TAX_RATES.put("ES", new TaxInfo(BigDecimal.valueOf(0.21), "VAT")); // Spain 21%
        TAX_RATES.put("UK", new TaxInfo(BigDecimal.valueOf(0.20), "VAT")); // UK 20%

        // North America
        TAX_RATES.put("US", new TaxInfo(BigDecimal.valueOf(0.00), "Sales Tax")); // Varies by state
        TAX_RATES.put("CA", new TaxInfo(BigDecimal.valueOf(0.13), "GST/HST")); // Canada varies by province

        // Africa
        TAX_RATES.put("TN", new TaxInfo(BigDecimal.valueOf(0.19), "VAT")); // Tunisia 19%
        TAX_RATES.put("MA", new TaxInfo(BigDecimal.valueOf(0.20), "VAT")); // Morocco 20%
        TAX_RATES.put("EG", new TaxInfo(BigDecimal.valueOf(0.14), "VAT")); // Egypt 14%

        // Middle East
        TAX_RATES.put("AE", new TaxInfo(BigDecimal.valueOf(0.05), "VAT")); // UAE 5%
        TAX_RATES.put("SA", new TaxInfo(BigDecimal.valueOf(0.15), "VAT")); // Saudi Arabia 15%

        // Asia
        TAX_RATES.put("CN", new TaxInfo(BigDecimal.valueOf(0.13), "VAT")); // China 13%
        TAX_RATES.put("JP", new TaxInfo(BigDecimal.valueOf(0.10), "Consumption Tax")); // Japan 10%
        TAX_RATES.put("IN", new TaxInfo(BigDecimal.valueOf(0.18), "GST")); // India 18%

        // Add more countries as needed
    }

    // US state tax rates (some examples)
    private static final Map<String, BigDecimal> US_STATE_TAX_RATES = new HashMap<>();

    static {
        US_STATE_TAX_RATES.put("CA", BigDecimal.valueOf(0.0725)); // California 7.25%
        US_STATE_TAX_RATES.put("NY", BigDecimal.valueOf(0.0400)); // New York 4%
        US_STATE_TAX_RATES.put("TX", BigDecimal.valueOf(0.0625)); // Texas 6.25%
        US_STATE_TAX_RATES.put("FL", BigDecimal.valueOf(0.0600)); // Florida 6%
        // Add more states as needed
    }

    public TaxCalculationResponse calculateTax(TaxCalculationRequest request) {
        String country = request.getCountry().toUpperCase();
        BigDecimal subtotal = request.getSubtotal();

        // Special handling for US (state-based tax)
        if ("US".equals(country) && request.getState() != null) {
            return calculateUSTax(subtotal, request.getState());
        }

        // Get tax info for country
        TaxInfo taxInfo = TAX_RATES.getOrDefault(country, new TaxInfo(BigDecimal.ZERO, "No Tax"));

        // Calculate tax amount
        BigDecimal taxAmount = subtotal
                .multiply(taxInfo.rate)
                .setScale(2, RoundingMode.HALF_UP);

        return TaxCalculationResponse.builder()
                .taxAmount(taxAmount)
                .taxRate(taxInfo.rate)
                .taxType(taxInfo.type)
                .build();
    }

    private TaxCalculationResponse calculateUSTax(BigDecimal subtotal, String state) {
        BigDecimal rate = US_STATE_TAX_RATES.getOrDefault(state.toUpperCase(), BigDecimal.ZERO);
        BigDecimal taxAmount = subtotal
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        return TaxCalculationResponse.builder()
                .taxAmount(taxAmount)
                .taxRate(rate)
                .taxType("Sales Tax")
                .build();
    }

    // Helper class to store tax info
    private static class TaxInfo {
        BigDecimal rate;
        String type;

        TaxInfo(BigDecimal rate, String type) {
            this.rate = rate;
            this.type = type;
        }
    }
}