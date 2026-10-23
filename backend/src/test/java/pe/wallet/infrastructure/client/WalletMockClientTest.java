package pe.wallet.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.wallet.infrastructure.client.generated.model.Health;
import pe.wallet.infrastructure.client.generated.model.PendingResolution;
import pe.wallet.infrastructure.client.generated.model.PostPaymentsRequest;

@QuarkusTest
@QuarkusTestResource(value = WalletMockServer.class, restrictToAnnotatedClass = true)
class WalletMockClientTest {

    @Inject
    WalletMockClient client;

    @Inject
    ObjectMapper mapper;

    HttpServer server;

    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicInteger requestCount = new AtomicInteger();
    private final AtomicReference<String> payload = new AtomicReference<>();
    private final AtomicReference<String> contentType = new AtomicReference<>("application/json");
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> query = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> latency = new AtomicReference<>();
    private final AtomicReference<String> idempotencyKey = new AtomicReference<>();

    @BeforeEach
    void startScenario() {
        server.createContext("/", exchange -> {
            requestCount.incrementAndGet();
            method.set(exchange.getRequestMethod());
            requestPath.set(exchange.getRequestURI().getRawPath());
            query.set(exchange.getRequestURI().getRawQuery());
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            latency.set(exchange.getRequestHeaders().getFirst("X-Mock-Latency-Ms"));
            idempotencyKey.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            byte[] response = payload.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType.get());
            exchange.sendResponseHeaders(status.get(), response.length);
            try (var stream = exchange.getResponseBody()) {
                stream.write(response);
            }
        });
    }

    @AfterEach
    void finishScenario() {
        server.removeContext("/");
    }

    @Test
    void shouldDeserializeHealthAndSendLatencyHeader() {
        payload.set("{\"status\":\"ok\",\"storage\":\"memory\"}");

        Health health = client.service().getHealth().await().atMost(Duration.ofSeconds(5));

        assertEquals(Health.StatusEnum.OK, health.getStatus());
        assertEquals(Health.StorageEnum.MEMORY, health.getStorage());
        assertEquals("GET", method.get());
        assertEquals("/groups/4/health", requestPath.get());
        assertEquals("0", latency.get());
    }

    @Test
    void shouldGetWalletUsingPathParameter() {
        payload.set("{\"id\":\"wallet-demo\",\"status\":\"ACTIVE\",\"balance\":100000,\"reserved\":100,\"available\":99900,\"currency\":\"PEN\"}");

        var wallet = client.onboarding().getWalletsByid("wallet-demo").await().atMost(Duration.ofSeconds(5));

        assertEquals("wallet-demo", wallet.getId());
        assertEquals(99900, wallet.getAvailable());
        assertEquals("/groups/4/wallets/wallet-demo", requestPath.get());
    }

    @Test
    void shouldSendPaymentJsonAndIdempotencyKey() throws Exception {
        status.set(202);
        payload.set(paymentJson());
        PostPaymentsRequest payment = new PostPaymentsRequest()
                .walletId("wallet-demo").recipient(PostPaymentsRequest.RecipientEnum.CONTACT_DEMO)
                .amountCents(100).currency(PostPaymentsRequest.CurrencyEnum.PEN)
                .scenario(PostPaymentsRequest.ScenarioEnum.APPROVED);

        var response = client.payments().postPayments("test-key", payment).await().atMost(Duration.ofSeconds(5));

        assertEquals("pay-test", response.getId());
        assertEquals("POST", method.get());
        assertEquals("/groups/4/payments", requestPath.get());
        assertEquals("test-key", idempotencyKey.get());
        assertEquals("wallet-demo", mapper.readTree(body.get()).get("walletId").asText());
        assertEquals(100, mapper.readTree(body.get()).get("amountCents").asInt());
        assertEquals(1, requestCount.get());
    }

    @Test
    void shouldSendQueryParameters() {
        payload.set("[]");

        var payments = client.payments().getPayments("wallet-demo").await().atMost(Duration.ofSeconds(5));

        assertTrue(payments.isEmpty());
        assertEquals("/groups/4/payments", requestPath.get());
        assertEquals("walletId=wallet-demo", query.get());
    }

    @Test
    void shouldRouteGlobalGroupsToRootServer() {
        payload.set("[]");

        client.service().groupsGet().await().atMost(Duration.ofSeconds(5));

        assertEquals("/groups", requestPath.get());
        assertEquals("0", latency.get());
    }

    @Test
    void shouldRouteGlobalUsageToRootServer() {
        payload.set("{\"totalRequests\":10,\"errors\":0,\"groups\":[]}");

        client.service().usageAllGet().await().atMost(Duration.ofSeconds(5));

        assertEquals("/usage/all", requestPath.get());
    }

    @Test
    void shouldPreserveNotFoundError() {
        status.set(404);
        payload.set("{\"error\":\"NOT_FOUND\"}");

        WalletApiException exception = assertThrows(WalletApiException.class,
                () -> client.onboarding().getWalletsByid("missing").await().atMost(Duration.ofSeconds(5)));

        assertEquals(404, exception.getStatusCode());
        assertEquals("NOT_FOUND", exception.getApiError().getError());
        assertEquals(1, requestCount.get());
    }

    @Test
    void shouldPreserveProcessedTimeoutWithoutRetrying() {
        status.set(504);
        payload.set("{\"error\":\"TIMEOUT_AFTER_PROCESSING\",\"processed\":true,\"paymentId\":\"pay-test\",\"status\":\"PENDING\"}");

        WalletApiException exception = assertThrows(WalletApiException.class,
                () -> client.payments().postPayments("timeout-key", new PostPaymentsRequest()
                        .walletId("wallet-demo").recipient(PostPaymentsRequest.RecipientEnum.CONTACT_DEMO)
                        .amountCents(100)).await().atMost(Duration.ofSeconds(5)));

        assertEquals(504, exception.getStatusCode());
        assertEquals("pay-test", exception.getApiError().getPaymentId());
        assertEquals(Boolean.TRUE, exception.getApiError().getProcessed());
        assertEquals(1, requestCount.get());
    }

    @Test
    void shouldHandleNonJsonProviderError() {
        status.set(503);
        contentType.set("text/html");
        payload.set("<html>Unavailable</html>");

        WalletApiException exception = assertThrows(WalletApiException.class,
                () -> client.service().getHealth().await().atMost(Duration.ofSeconds(5)));

        assertEquals(503, exception.getStatusCode());
        assertEquals("UNEXPECTED_HTTP_ERROR", exception.getApiError().getError());
    }

    @Test
    void shouldExposePendingResolutionWithItsActualStatusAndSchema() {
        status.set(202);
        payload.set("{\"status\":\"PENDING\",\"escalated\":true}");

        try (Response response = client.failures().postPaymentsByidResolveResponse("pay-pending", null)
                .await().atMost(Duration.ofSeconds(5))) {
            assertEquals(202, response.getStatus());
            PendingResolution pending = response.readEntity(PendingResolution.class);
            assertEquals(Boolean.TRUE, pending.getEscalated());
            assertNotNull(pending.getStatus());
        }
    }

    private String paymentJson() {
        return """
                {"id":"pay-test","wallet_id":"wallet-demo","recipient":"contact-demo",
                 "amount":100,"status":"PENDING","scenario":"APPROVED","idem":"test-key",
                 "fingerprint":"cloud-fingerprint"}
                """;
    }
}