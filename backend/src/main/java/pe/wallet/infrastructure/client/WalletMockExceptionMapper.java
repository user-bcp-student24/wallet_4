package pe.wallet.infrastructure.client;

import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import pe.wallet.infrastructure.client.generated.model.Error;

@Blocking
public class WalletMockExceptionMapper implements ResponseExceptionMapper<WalletApiException> {

    @Override
    public WalletApiException toThrowable(Response response) {
        Error error = null;
        if (response.hasEntity() && response.getMediaType() != null
                && response.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            try {
                error = response.readEntity(Error.class);
            } catch (ProcessingException | IllegalStateException ignored) {
                error = null;
            }
        }
        if (error == null || error.getError() == null) {
            error = new Error().error("UNEXPECTED_HTTP_ERROR");
        }
        return new WalletApiException(response.getStatus(), error);
    }

    @Override
    public int getPriority() {
        return 100;
    }
}