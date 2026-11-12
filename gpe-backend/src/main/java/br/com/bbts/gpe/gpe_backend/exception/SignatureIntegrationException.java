package br.com.bbts.gpe.gpe_backend.exception;

public class SignatureIntegrationException extends RuntimeException {
    public SignatureIntegrationException(String message) {
        super(message);
    }

    public SignatureIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
