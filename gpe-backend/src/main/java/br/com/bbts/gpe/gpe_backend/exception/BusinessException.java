package br.com.bbts.gpe.gpe_backend.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}