package pe.wallet.infrastructure.client;

import pe.wallet.infrastructure.client.generated.model.Error;

public class WalletApiException extends RuntimeException {

    private final int statusCode;
    private final Error apiError;

    public WalletApiException(int statusCode, Error apiError) {
        super("Wallet API HTTP " + statusCode + ": " + apiError.getError());
        this.statusCode = statusCode;
        this.apiError = apiError;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Error getApiError() {
        return apiError;
    }
}