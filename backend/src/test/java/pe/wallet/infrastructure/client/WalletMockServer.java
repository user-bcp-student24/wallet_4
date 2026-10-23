package pe.wallet.infrastructure.client;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class WalletMockServer implements QuarkusTestResourceLifecycleManager {

    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.start();
            String root = "http://127.0.0.1:" + server.getAddress().getPort();
            return Map.of("quarkus.rest-client.wallet-mock.url", root + "/groups/4",
                    "wallet.mock.root-url", root);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot start local wallet test server", exception);
        }
    }

    @Override
    public void inject(TestInjector injector) {
        injector.injectIntoFields(server, new TestInjector.MatchesType(HttpServer.class));
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}