package com.ordershub.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PagarMeClient {

    private final RestClient restClient;
    private final String apiKey;

    public PagarMeClient(@Value("${app.pagarme.base-url}") String baseUrl,
                         @Value("${app.pagarme.api-key}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public record PaymentResult(String status, String id) {}

    public PaymentResult createOrder(Long orderId, java.math.BigDecimal amount, String cardNumber) {
        Map<String, Object> body = Map.of(
                "amount", amount.movePointRight(2).intValue(), // em centavos
                "payments", java.util.List.of(Map.of(
                        "payment_method", "credit_card",
                        "credit_card", Map.of(
                                "installments", 1,
                                "card", Map.of("number", cardNumber, "holder_name", "Teste")
                        )
                ))
        );

        Map<String, Object> response = restClient.post()
                .uri("/orders")
                .header("Authorization", "Basic " + java.util.Base64.getEncoder()
                        .encodeToString((apiKey + ":").getBytes()))
                .body(body)
                .retrieve()
                .body(Map.class);

        return new PaymentResult((String) response.get("status"), (String) response.get("id"));
    }
}