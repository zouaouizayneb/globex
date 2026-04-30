package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.fst.backend.backend.dto.PaymentInitiateRequest;
import tn.fst.backend.backend.dto.PaymentInitiateResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de paiement via l'API Flouci v2.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlouciPaymentService {

    @Value("${flouci.app.public:d7c3505...}")
    private String appPublic;

    @Value("${flouci.app.secret:secret...}")
    private String appSecret;

    @Value("${flouci.api.url:https://developers.flouci.com/api/v2}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentInitiateResponse createPayment(PaymentInitiateRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(appPublic, appSecret); // Some APIs use Basic, Flouci needs Bearer <public>:<secret>
            // Actually, Flouci documentation says: Authorization: Bearer <APP_PUBLIC>:<APP_SECRET>
            headers.set("Authorization", "Bearer " + appPublic + ":" + appSecret);

            Map<String, Object> body = new HashMap<>();
            // The amount MUST be an integer representing millimes in Tunisia (1 TND = 1000 millimes)
            String amountInMillimes = String.valueOf(request.getAmount().multiply(new BigDecimal("1000")).intValue());
            body.put("amount", amountInMillimes);
            
            // Tracking ID used by us (e.g. orderId)
            body.put("developer_tracking_id", request.getOrderId());
            body.put("accept_card", true);
            body.put("success_link", request.getReturnUrl() + "?method=FLOUCI");
            body.put("fail_link", request.getCancelUrl() + "?method=FLOUCI");
            body.put("client_id", request.getOrderId()); // Optional mapping

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl + "/generate_payment", entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
                String link = (String) result.get("link");
                String paymentId = (String) result.get("payment_id");

                return PaymentInitiateResponse.builder()
                        .paymentUrl(link)
                        .transactionId(paymentId)
                        .status("PENDING")
                        .message("Flouci payment generated")
                        .build();
            }

            throw new RuntimeException("Invalid Flouci Response");

        } catch (Exception e) {
            log.error("Error creating Flouci payment", e);
            throw new RuntimeException("Could not contact Flouci Payment Gateway: " + e.getMessage());
        }
    }

    public String verifyPayment(String paymentId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + appPublic + ":" + appSecret);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/verify_payment/" + paymentId,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && Boolean.TRUE.equals(body.get("success"))) {
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                if (result != null && "SUCCESS".equalsIgnoreCase((String) result.get("status"))) {
                    return "SUCCESS";
                }
            }
            return "FAILED";
        } catch (Exception e) {
            log.error("Error verifying Flouci payment", e);
            return "FAILED";
        }
    }
}
