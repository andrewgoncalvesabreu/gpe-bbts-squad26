package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.dto.AssinaturaDocumentoDTO;
import br.com.bbts.gpe.gpe_backend.dto.AssinaturaRequestDTO;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoDTO;
import br.com.bbts.gpe.gpe_backend.dto.DocumentoVersaoDTO;
import br.com.bbts.gpe.gpe_backend.service.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DocumentoController {
    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping(value = "/parceiros/{parceiroId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> criar(
            @PathVariable UUID parceiroId,
            @RequestParam("parceriaId") UUID parceriaId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("responsavelMatricula") String responsavelMatricula,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "descricao", required = false) String descricao) {
        return ResponseEntity.status(201)
                .body(documentoService.criar(parceiroId, parceriaId, nome, descricao, responsavelMatricula, file));
    }

    @GetMapping("/parceiros/{parceiroId}/documentos")
    public ResponseEntity<List<DocumentoDTO>> listarPorParceiro(@PathVariable UUID parceiroId) {
        return ResponseEntity.ok(documentoService.listarPorParceiro(parceiroId));
    }

    @GetMapping("/documentos/{documentoId}")
    public ResponseEntity<Resource> baixarAtual(@PathVariable UUID documentoId) {
        return respostaArquivo(documentoService.obterArquivo(documentoId,
                documentoService.buscar(documentoId).versaoAtual()));
    }

    @GetMapping("/documentos/{documentoId}/versoes")
    public ResponseEntity<List<DocumentoVersaoDTO>> listarVersoes(@PathVariable UUID documentoId) {
        return ResponseEntity.ok(documentoService.listarVersoes(documentoId));
    }

    @GetMapping("/documentos/{documentoId}/versoes/{numeroVersao}/arquivo")
    public ResponseEntity<Resource> baixarVersao(
            @PathVariable UUID documentoId,
            @PathVariable Integer numeroVersao) {
        return respostaArquivo(documentoService.obterArquivo(documentoId, numeroVersao));
    }

    @PostMapping(value = "/documentos/{documentoId}/versoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> criarVersao(
            @PathVariable UUID documentoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("responsavelMatricula") String responsavelMatricula) {
        return ResponseEntity.ok(documentoService.criarVersao(documentoId, responsavelMatricula, file));
    }

    @PostMapping("/documentos/{documentoId}/assinaturas")
    public ResponseEntity<AssinaturaDocumentoDTO> enviarParaAssinatura(
            @PathVariable UUID documentoId,
            @Valid @RequestBody AssinaturaRequestDTO request) {
        return ResponseEntity.accepted()
                .body(documentoService.enviarParaAssinatura(documentoId, request.responsavelMatricula()));
    }

    @GetMapping("/documentos/{documentoId}/assinaturas")
    public ResponseEntity<List<AssinaturaDocumentoDTO>> listarAssinaturas(@PathVariable UUID documentoId) {
        return ResponseEntity.ok(documentoService.listarAssinaturas(documentoId));
    }

    private ResponseEntity<Resource> respostaArquivo(DocumentoService.DocumentDownload file) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }
}
