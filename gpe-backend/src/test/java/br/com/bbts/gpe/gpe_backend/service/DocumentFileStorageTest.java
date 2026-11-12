package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.exception.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentFileStorageTest {
    @TempDir
    Path storageDirectory;

    @Test
    void storesPdfWithGeneratedKeyAndSha256() throws Exception {
        DocumentFileStorage storage = new DocumentFileStorage(storageDirectory.toString());
        byte[] content = "%PDF-1.7\nsample".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", content);

        StoredDocumentFile stored = storage.store(file);
        Resource resource = storage.load(stored.storageKey());

        assertEquals("application/pdf", stored.mimeType());
        assertEquals(content.length, stored.size());
        assertEquals(64, stored.sha256().length());
        assertArrayEquals(content, resource.getInputStream().readAllBytes());
        assertTrue(Files.exists(storageDirectory.resolve(stored.storageKey())));
        storage.delete(stored.storageKey());
        assertFalse(Files.exists(storageDirectory.resolve(stored.storageKey())));
    }

    @Test
    void rejectsUnsupportedExtensionAndContentSpoofing() {
        DocumentFileStorage storage = new DocumentFileStorage(storageDirectory.toString());
        MockMultipartFile unsupported = new MockMultipartFile("file", "script.exe",
                "application/octet-stream", new byte[] {1, 2, 3});
        MockMultipartFile spoofedPdf = new MockMultipartFile("file", "fake.pdf",
                "application/pdf", "not a pdf".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThrows(InvalidFileException.class, () -> storage.store(unsupported));
        assertThrows(InvalidFileException.class, () -> storage.store(spoofedPdf));
    }

    @Test
    void rejectsFilesOverTwentyMegabytes() {
        DocumentFileStorage storage = new DocumentFileStorage(storageDirectory.toString());
        byte[] content = new byte[(int) DocumentFileStorage.MAX_SIZE_BYTES + 1];
        MockMultipartFile oversized = new MockMultipartFile("file", "large.pdf",
                "application/pdf", content);

        assertThrows(InvalidFileException.class, () -> storage.store(oversized));
    }
}
