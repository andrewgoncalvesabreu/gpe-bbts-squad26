package br.com.bbts.gpe.gpe_backend.service;

public record SignatureResult(
        String provider,
        String status,
        String protocol,
        String detail) {
}
