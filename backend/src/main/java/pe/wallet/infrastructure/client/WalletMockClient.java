package pe.wallet.infrastructure.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import pe.wallet.infrastructure.client.generated.api.ControlesDelMockClient;
import pe.wallet.infrastructure.client.generated.api.ServicioYGruposClient;
import pe.wallet.infrastructure.client.generated.api.W1RegistroYActivacinClient;
import pe.wallet.infrastructure.client.generated.api.W2PagoAContactoClient;
import pe.wallet.infrastructure.client.generated.api.W3PagoQrComercioClient;
import pe.wallet.infrastructure.client.generated.api.W4IdempotenciaYCallbacksClient;
import pe.wallet.infrastructure.client.generated.api.W5FallasYReversosClient;
import pe.wallet.infrastructure.client.generated.api.W6ConciliacinClient;

@ApplicationScoped
public class WalletMockClient {

    private final ServicioYGruposClient service;
    private final W1RegistroYActivacinClient onboarding;
    private final W2PagoAContactoClient payments;
    private final W3PagoQrComercioClient qr;
    private final W4IdempotenciaYCallbacksClient callbacks;
    private final W5FallasYReversosClient failures;
    private final W6ConciliacinClient reconciliation;
    private final ControlesDelMockClient controls;

    public WalletMockClient(@RestClient ServicioYGruposClient service,
            @RestClient W1RegistroYActivacinClient onboarding,
            @RestClient W2PagoAContactoClient payments,
            @RestClient W3PagoQrComercioClient qr,
            @RestClient W4IdempotenciaYCallbacksClient callbacks,
            @RestClient W5FallasYReversosClient failures,
            @RestClient W6ConciliacinClient reconciliation,
            @RestClient ControlesDelMockClient controls) {
        this.service = service;
        this.onboarding = onboarding;
        this.payments = payments;
        this.qr = qr;
        this.callbacks = callbacks;
        this.failures = failures;
        this.reconciliation = reconciliation;
        this.controls = controls;
    }

    public ServicioYGruposClient service() {
        return service;
    }

    public W1RegistroYActivacinClient onboarding() {
        return onboarding;
    }

    public W2PagoAContactoClient payments() {
        return payments;
    }

    public W3PagoQrComercioClient qr() {
        return qr;
    }

    public W4IdempotenciaYCallbacksClient callbacks() {
        return callbacks;
    }

    public W5FallasYReversosClient failures() {
        return failures;
    }

    public W6ConciliacinClient reconciliation() {
        return reconciliation;
    }

    public ControlesDelMockClient controls() {
        return controls;
    }
}