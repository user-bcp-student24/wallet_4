package pe.wallet.infrastructure.client;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class WalletMockRequestFilter implements ClientRequestFilter {

    private static final Set<String> ROOT_PATHS = Set.of("/groups", "/usage/all");
    private final String latency;
    private final URI rootUrl;

    public WalletMockRequestFilter(@ConfigProperty(name = "wallet.mock.latency-ms") String latency,
            @ConfigProperty(name = "wallet.mock.root-url") URI rootUrl) {
        this.latency = latency;
        this.rootUrl = rootUrl;
    }

    @Override
    public void filter(ClientRequestContext request) {
        request.getHeaders().putSingle("X-Mock-Latency-Ms", latency);
        Object invokedMethod = request.getProperty("org.eclipse.microprofile.rest.client.invokedMethod");
        if (invokedMethod instanceof Method method) {
            Path path = method.getAnnotation(Path.class);
            if (path != null && ROOT_PATHS.contains(path.value())) {
                request.setUri(UriBuilder.fromUri(rootUrl)
                        .path(path.value())
                        .replaceQuery(request.getUri().getRawQuery())
                        .build());
            }
        }
    }
}