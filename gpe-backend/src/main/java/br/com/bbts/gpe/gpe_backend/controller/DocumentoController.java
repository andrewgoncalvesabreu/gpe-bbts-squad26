package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.dto.DocumentoDTO;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoVersaoDTO;
import br.com.bbts.gpe.gpe_backend.service.DocumentoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> listar(
            @RequestParam(required = false) UUID parceiroId,
            @RequestParam(required = false) UUID oportunidadeId,
            @RequestParam(required = false) UUID instrumentoId) {

        return ResponseEntity.ok(
                documentoService.listar(
                        parceiroId,
                        oportunidadeId,
                        instrumentoId));
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentoDTO> criar(
            @RequestParam("nome") String nome,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "parceiroId", required = false) UUID parceiroId,
            @RequestParam(value = "oportunidadeId", required = false) UUID oportunidadeId,
            @RequestParam(value = "instrumentoId", required = false) UUID instrumentoId,
            @RequestParam("arquivo") MultipartFile arquivo) {

        DocumentoDTO documento = documentoService.criar(
                nome,
                descricao,
                parceiroId,
                oportunidadeId,
                instrumentoId,
                arquivo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documento);
    }

    @GetMapping("/{id}/versoes")
    public ResponseEntity<List<DocumentoVersaoDTO>> listarVersoes(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                documentoService.listarVersoes(id));
    }

    @PostMapping(
            value = "/{id}/versoes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentoVersaoDTO> adicionarVersao(
            @PathVariable UUID id,
            @RequestParam("arquivo") MultipartFile arquivo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentoService.adicionarVersao(id, arquivo));
    }

    @GetMapping("/{id}/versoes/{numeroVersao}/download")
    public ResponseEntity<Resource> baixar(
            @PathVariable UUID id,
            @PathVariable Integer numeroVersao) {

        DocumentoService.DownloadFile file =
                documentoService.baixar(id, numeroVersao);

        ContentDisposition disposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                file.fileName(),
                                StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity
                .ok()
                .contentType(parseContentType(file.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString())
                .body(file.resource());
    }

    private MediaType parseContentType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
