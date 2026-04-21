package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.ShippingRateRequest;
import tn.fst.backend.backend.dto.ShippingRateResponse;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShippingService {

    // Shipping zones for different regions
    private static final Map<String, ShippingZone> SHIPPING_ZONES = new HashMap<>();

    static {
        // Zone 1: Domestic (Tunisia)
        Set<String> zone1 = new HashSet<>(Arrays.asList("TN"));
        SHIPPING_ZONES.put("ZONE1", new ShippingZone("Zone 1 - Domestic", zone1,
                BigDecimal.valueOf(5.00),    // Standard
                BigDecimal.valueOf(10.00),   // Express
                BigDecimal.valueOf(20.00))); // Overnight

        // Zone 2: North Africa & Middle East
        Set<String> zone2 = new HashSet<>(Arrays.asList("DZ", "MA", "LY", "EG", "SA", "AE", "QA"));
        SHIPPING_ZONES.put("ZONE2", new ShippingZone("Zone 2 - MENA", zone2,
                BigDecimal.valueOf(15.00),   // Standard
                BigDecimal.valueOf(30.00),   // Express
                BigDecimal.valueOf(50.00))); // Overnight

        // Zone 3: Europe
        Set<String> zone3 = new HashSet<>(Arrays.asList("FR", "DE", "IT", "ES", "UK", "NL", "BE"));
        SHIPPING_ZONES.put("ZONE3", new ShippingZone("Zone 3 - Europe", zone3,
                BigDecimal.valueOf(20.00),   // Standard
                BigDecimal.valueOf(40.00),   // Express
                BigDecimal.valueOf(70.00))); // Overnight

        // Zone 4: North America
        Set<String> zone4 = new HashSet<>(Arrays.asList("US", "CA", "MX"));
        SHIPPING_ZONES.put("ZONE4", new ShippingZone("Zone 4 - North America", zone4,
                BigDecimal.valueOf(30.00),   // Standard
                BigDecimal.valueOf(60.00),   // Express
                BigDecimal.valueOf(100.00))); // Overnight

        // Zone 5: Asia & Rest of World
        Set<String> zone5 = new HashSet<>(Arrays.asList("CN", "JP", "IN", "AU", "BR"));
        SHIPPING_ZONES.put("ZONE5", new ShippingZone("Zone 5 - Asia & ROW", zone5,
                BigDecimal.valueOf(35.00),   // Standard
                BigDecimal.valueOf(70.00),   // Express
                BigDecimal.valueOf(120.00))); // Overnight
    }

    public List<ShippingRateResponse> getAvailableShippingRates(String country) {
        ShippingZone zone = getShippingZone(country);
        List<ShippingRateResponse> rates = new ArrayList<>();

        // Standard Shipping
        rates.add(ShippingRateResponse.builder()
                .method("STANDARD")
                .cost(zone.standardCost)
                .carrier("Standard Carrier")
                .estimatedDays(7)
                .description("Standard shipping (5-7 business days)")
                .build());

        // Express Shipping
        rates.add(ShippingRateResponse.builder()
                .method("EXPRESS")
                .cost(zone.expressCost)
                .carrier("Express Carrier")
                .estimatedDays(3)
                .description("Express shipping (2-3 business days)")
                .build());

        // Overnight (only for certain zones)
        if (!zone.name.contains("ROW")) {
            rates.add(ShippingRateResponse.builder()
                    .method("OVERNIGHT")
                    .cost(zone.overnightCost)
                    .carrier("Premium Carrier")
                    .estimatedDays(1)
                    .description("Overnight shipping (next business day)")
                    .build());
        }

        return rates;
    }

    public ShippingRateResponse calculateShippingCost(ShippingRateRequest request) {
        String country = request.getDestinationCountry().toUpperCase();
        String method = request.getShippingMethod().toUpperCase();
        BigDecimal weight = request.getWeight() != null ? request.getWeight() : BigDecimal.ONE;

        ShippingZone zone = getShippingZone(country);
        BigDecimal baseCost;
        String carrier;
        int estimatedDays;
        String description;

        switch (method) {
            case "STANDARD":
                baseCost = zone.standardCost;
                carrier = "Standard Carrier";
                estimatedDays = 7;
                description = "Standard shipping (5-7 business days)";
                break;
            case "EXPRESS":
                baseCost = zone.expressCost;
                carrier = "Express Carrier";
                estimatedDays = 3;
                description = "Express shipping (2-3 business days)";
                break;
            case "OVERNIGHT":
                baseCost = zone.overnightCost;
                carrier = "Premium Carrier";
                estimatedDays = 1;
                description = "Overnight shipping (next business day)";
                break;
            default:
                throw new IllegalArgumentException("Invalid shipping method: " + method);
        }

        // Calculate cost based on weight (add $2 per kg over 1kg)
        BigDecimal weightSurcharge = BigDecimal.ZERO;
        if (weight.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal extraWeight = weight.subtract(BigDecimal.ONE);
            weightSurcharge = extraWeight.multiply(BigDecimal.valueOf(2.00));
        }

        BigDecimal totalCost = baseCost.add(weightSurcharge);

        return ShippingRateResponse.builder()
                .method(method)
                .cost(totalCost)
                .carrier(carrier)
                .estimatedDays(estimatedDays)
                .description(description)
                .build();
    }

    private ShippingZone getShippingZone(String country) {
        for (ShippingZone zone : SHIPPING_ZONES.values()) {
            if (zone.countries.contains(country)) {
                return zone;
            }
        }
        // Default to Zone 5 (most expensive) if country not found
        return SHIPPING_ZONES.get("ZONE5");
    }

    // Helper class for shipping zones
    private static class ShippingZone {
        String name;
        Set<String> countries;
        BigDecimal standardCost;
        BigDecimal expressCost;
        BigDecimal overnightCost;

        ShippingZone(String name, Set<String> countries, BigDecimal standard, BigDecimal express, BigDecimal overnight) {
            this.name = name;
            this.countries = countries;
            this.standardCost = standard;
            this.expressCost = express;
            this.overnightCost = overnight;
        }
    }
}