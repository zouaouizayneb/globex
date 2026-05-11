package tn.fst.backend.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xbill.DNS.*;

import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Validate email format and check if domain has valid MX records
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Check email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            log.warn("Invalid email format: {}", email);
            return false;
        }

        // Extract domain
        String domain = email.substring(email.indexOf('@') + 1);

        // Check MX records
        return hasValidMXRecords(domain);
    }

    /**
     * Check if domain has valid MX records
     */
    private boolean hasValidMXRecords(String domain) {
        try {
            Lookup lookup = new Lookup(domain, Type.MX);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null); // Disable cache to get fresh results

            org.xbill.DNS.Record[] records = lookup.run();

            if (records == null || records.length == 0) {
                log.warn("No MX records found for domain: {}", domain);
                return false;
            }

            log.info("Found {} MX records for domain: {}", records.length, domain);
            return true;

        } catch (TextParseException e) {
            log.error("DNS lookup failed for domain {}: {}", domain, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during DNS lookup for domain {}: {}", domain, e.getMessage());
            // Return true to not block registration if DNS lookup fails
            // In production, you might want to handle this differently
            return true;
        }
    }
}
