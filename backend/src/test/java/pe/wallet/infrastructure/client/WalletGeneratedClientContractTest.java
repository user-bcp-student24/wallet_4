package pe.wallet.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pe.wallet.infrastructure.client.generated.api.ControlesDelMockClient;
import pe.wallet.infrastructure.client.generated.api.ServicioYGruposClient;
import pe.wallet.infrastructure.client.generated.api.W1RegistroYActivacinClient;
import pe.wallet.infrastructure.client.generated.api.W2PagoAContactoClient;
import pe.wallet.infrastructure.client.generated.api.W3PagoQrComercioClient;
import pe.wallet.infrastructure.client.generated.api.W4IdempotenciaYCallbacksClient;
import pe.wallet.infrastructure.client.generated.api.W5FallasYReversosClient;
import pe.wallet.infrastructure.client.generated.api.W6ConciliacinClient;

class WalletGeneratedClientContractTest {

    @Test
    void shouldGenerateReactiveTypedAndRawMethodsForEveryContractOperation() throws Exception {
        Set<String> expected = new HashSet<>();
        Set<String> verbs = Set.of("get", "post", "put", "patch", "delete", "head", "options", "trace");
        try (var stream = getClass().getResourceAsStream("/META-INF/resources/contracts/openapi.json")) {
            assertNotNull(stream);
            var paths = new ObjectMapper().readTree(stream).get("paths");
            paths.fields().forEachRemaining(entry -> entry.getValue().fieldNames().forEachRemaining(verb -> {
                if (verbs.contains(verb)) {
                    expected.add(verb.toUpperCase(java.util.Locale.ROOT) + " " + entry.getKey());
                }
            }));
        }

        Set<String> typedOperations = new HashSet<>();
        Set<String> rawOperations = new HashSet<>();
        List<Class<?>> clients = List.of(ServicioYGruposClient.class, W1RegistroYActivacinClient.class,
                W2PagoAContactoClient.class, W3PagoQrComercioClient.class,
                W4IdempotenciaYCallbacksClient.class, W5FallasYReversosClient.class,
                W6ConciliacinClient.class, ControlesDelMockClient.class);

        for (Class<?> client : clients) {
            String basePath = client.getAnnotation(Path.class).value();
            for (var method : client.getDeclaredMethods()) {
                var verb = Arrays.stream(method.getAnnotations())
                        .map(annotation -> annotation.annotationType().getAnnotation(HttpMethod.class))
                        .filter(java.util.Objects::nonNull).findFirst().orElseThrow();
                var methodPath = method.getAnnotation(Path.class);
                String route = basePath + (methodPath == null ? "" : methodPath.value());
                assertEquals(Uni.class, method.getReturnType(), method.getName());
                String operation = verb.value() + " " + route;
                if (method.getName().endsWith("Response")) {
                    rawOperations.add(operation);
                } else {
                    typedOperations.add(operation);
                }
            }
        }

        assertEquals(expected, typedOperations, "Typed SDK coverage differs from OpenAPI");
        assertEquals(expected, rawOperations, "Raw SDK coverage differs from OpenAPI");
    }
}