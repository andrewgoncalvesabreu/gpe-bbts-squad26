package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.exception.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArquivoDocumentoStorageServiceTest {
    @TempDir
    Path storageDirectory;

    @Test
    void storesPdfWithGeneratedPathAndSha256() throws Exception {
        ArquivoDocumentoStorageService storage =
                new ArquivoDocumentoStorageService(storageDirectory.toString());
        UUID documentoId = UUID.randomUUID();
        byte[] content = "%PDF-1.7\nsample".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile("arquivo",
                "C:\\documentos\\contrato.pdf", "application/pdf", content);

        ArquivoDocumentoStorageService.StoredFile stored = storage.store(documentoId, 1, file);
        Resource resource = storage.load(stored.path());

        assertEquals("contrato.pdf", stored.originalName());
        assertEquals("application/pdf", stored.contentType());
        assertEquals(content.length, stored.size());
        assertEquals(64, stored.sha256().length());
        try (var input = resource.getInputStream()) {
            assertArrayEquals(content, input.readAllBytes());
        }
        assertTrue(Files.exists(storageDirectory.resolve(stored.path())));
        storage.delete(stored.path());
        assertFalse(Files.exists(storageDirectory.resolve(stored.path())));
    }

    @Test
    void rejectsUnsupportedExtensionAndContentSpoofing() {
        ArquivoDocumentoStorageService storage =
                new ArquivoDocumentoStorageService(storageDirectory.toString());
        UUID documentoId = UUID.randomUUID();
        MockMultipartFile unsupported = new MockMultipartFile("arquivo", "script.exe",
                "application/octet-stream", new byte[] {1, 2, 3});
        MockMultipartFile spoofedPdf = new MockMultipartFile("arquivo", "fake.pdf",
                "application/pdf", "not a pdf".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidFileException.class, () -> storage.store(documentoId, 1, unsupported));
        assertThrows(InvalidFileException.class, () -> storage.store(documentoId, 1, spoofedPdf));
    }

    @Test
    void rejectsFilesOverTwentyMegabytes() {
        ArquivoDocumentoStorageService storage =
                new ArquivoDocumentoStorageService(storageDirectory.toString());
        byte[] content = new byte[(int) ArquivoDocumentoStorageService.MAX_SIZE_BYTES + 1];
        MockMultipartFile oversized = new MockMultipartFile("arquivo", "large.pdf",
                "application/pdf", content);

        assertThrows(InvalidFileException.class,
                () -> storage.store(UUID.randomUUID(), 1, oversized));
    }
}
