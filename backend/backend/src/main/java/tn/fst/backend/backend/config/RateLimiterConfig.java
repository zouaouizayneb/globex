package tn.fst.backend.backend.config;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterConfig {

    private final Map<String, LoginAttemptRecord> attemptRecords = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW_MINUTES = 15;

    private static class LoginAttemptRecord {
        int attemptCount;
        long firstAttemptTime;

        LoginAttemptRecord() {
            this.attemptCount = 1;
            this.firstAttemptTime = System.currentTimeMillis();
        }
    }

    /**
     * Check if the request is allowed for the given key (IP address or email)
     * Rate limit: 5 attempts per 15 minutes
     */
    public boolean tryConsume(String key) {
        long currentTime = System.currentTimeMillis();
        LoginAttemptRecord record = attemptRecords.get(key);

        if (record == null) {
            attemptRecords.put(key, new LoginAttemptRecord());
            return true;
        }

        long timeSinceFirstAttempt = currentTime - record.firstAttemptTime;
        long timeWindowMillis = TimeUnit.MINUTES.toMillis(TIME_WINDOW_MINUTES);

        if (timeSinceFirstAttempt > timeWindowMillis) {
            // Reset if time window has expired
            attemptRecords.put(key, new LoginAttemptRecord());
            return true;
        }

        if (record.attemptCount >= MAX_ATTEMPTS) {
            return false;
        }

        record.attemptCount++;
        return true;
    }

    /**
     * Get remaining attempts for the given key
     */
    public long getRemainingTokens(String key) {
        LoginAttemptRecord record = attemptRecords.get(key);
        if (record == null) {
            return MAX_ATTEMPTS;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceFirstAttempt = currentTime - record.firstAttemptTime;
        long timeWindowMillis = TimeUnit.MINUTES.toMillis(TIME_WINDOW_MINUTES);

        if (timeSinceFirstAttempt > timeWindowMillis) {
            return MAX_ATTEMPTS;
        }

        return Math.max(0, MAX_ATTEMPTS - record.attemptCount);
    }

    /**
     * Clear the rate limit record for a specific key (useful for testing or manual reset)
     */
    public void clearRecord(String key) {
        attemptRecords.remove(key);
    }
}
