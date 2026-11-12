package br.com.bbts.gpe.gpe_backend.service;

public record StoredDocumentFile(
        String storageKey,
        String mimeType,
        long size,
        String sha256) {
}
