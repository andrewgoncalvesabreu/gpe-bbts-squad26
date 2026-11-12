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

    @GetMapping("/documentos")
    public ResponseEntity<List<DocumentoDTO>> listar(
            @RequestParam(required = false) UUID parceiroId,
            @RequestParam(required = false) UUID oportunidadeId,
            @RequestParam(required = false) UUID instrumentoId,
            @RequestParam(required = false) UUID parceriaId) {
        return ResponseEntity.ok(documentoService.listar(parceiroId, oportunidadeId, instrumentoId, parceriaId));
    }

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> criar(
            @RequestParam("nome") String nome,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "parceiroId", required = false) UUID parceiroId,
            @RequestParam(value = "oportunidadeId", required = false) UUID oportunidadeId,
            @RequestParam(value = "instrumentoId", required = false) UUID instrumentoId,
            @RequestParam(value = "parceriaId", required = false) UUID parceriaId,
            @RequestParam(value = "responsavelMatricula", required = false) String responsavelMatricula,
            @RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.status(201).body(documentoService.criar(
                nome, descricao, parceiroId, oportunidadeId, instrumentoId,
                parceriaId, responsavelMatricula, arquivo));
    }

    @GetMapping("/parceiros/{parceiroId}/documentos")
    public ResponseEntity<List<DocumentoDTO>> listarPorParceiro(@PathVariable UUID parceiroId) {
        return ResponseEntity.ok(documentoService.listarPorParceiro(parceiroId));
    }

    @PostMapping(value = "/parceiros/{parceiroId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> criarParaParceria(
            @PathVariable UUID parceiroId,
            @RequestParam("parceriaId") UUID parceriaId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("responsavelMatricula") String responsavelMatricula,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "descricao", required = false) String descricao) {
        String documentName = nome == null || nome.isBlank() ? file.getOriginalFilename() : nome;
        return ResponseEntity.status(201).body(documentoService.criarParaParceria(
                parceiroId, parceriaId, documentName, descricao, responsavelMatricula, file));
    }

    @GetMapping("/documentos/{documentoId}")
    public ResponseEntity<Resource> baixarAtual(@PathVariable UUID documentoId) {
        return respostaArquivo(documentoService.baixarAtual(documentoId));
    }

    @GetMapping("/documentos/{id}/versoes")
    public ResponseEntity<List<DocumentoVersaoDTO>> listarVersoes(@PathVariable UUID id) {
        return ResponseEntity.ok(documentoService.listarVersoes(id));
    }

    @PostMapping(value = "/documentos/{id}/versoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoVersaoDTO> adicionarVersao(
            @PathVariable UUID id,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "responsavelMatricula", required = false) String responsavelMatricula) {
        return ResponseEntity.status(201)
                .body(documentoService.adicionarVersao(id, arquivo, responsavelMatricula));
    }

    @GetMapping({
            "/documentos/{id}/versoes/{numeroVersao}/download",
            "/documentos/{id}/versoes/{numeroVersao}/arquivo"
    })
    public ResponseEntity<Resource> baixarVersao(
            @PathVariable UUID id,
            @PathVariable Integer numeroVersao) {
        return respostaArquivo(documentoService.baixar(id, numeroVersao));
    }

    @PostMapping("/documentos/{id}/assinaturas")
    public ResponseEntity<AssinaturaDocumentoDTO> enviarParaAssinatura(
            @PathVariable UUID id,
            @Valid @RequestBody AssinaturaRequestDTO request) {
        return ResponseEntity.accepted()
                .body(documentoService.enviarParaAssinatura(id, request.responsavelMatricula()));
    }

    @GetMapping("/documentos/{id}/assinaturas")
    public ResponseEntity<List<AssinaturaDocumentoDTO>> listarAssinaturas(@PathVariable UUID id) {
        return ResponseEntity.ok(documentoService.listarAssinaturas(id));
    }

    private ResponseEntity<Resource> respostaArquivo(DocumentoService.DownloadFile file) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }
}
