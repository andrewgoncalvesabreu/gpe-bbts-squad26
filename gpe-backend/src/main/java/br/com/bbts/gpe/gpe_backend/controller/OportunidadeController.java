package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.AvaliacaoOportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.dto.AvaliacaoRequestDTO;
import br.com.bbts.gpe.gpe_backend.dto.WorkflowDecisionDTO;
import br.com.bbts.gpe.gpe_backend.service.OportunidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/oportunidades")
public class OportunidadeController {

    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    @GetMapping
    public ResponseEntity<List<Oportunidade>> listar(
            @RequestParam(required = false) UUID parceiroId) {
        return ResponseEntity.ok(oportunidadeService.listar(parceiroId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Oportunidade> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(oportunidadeService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Oportunidade> criar(@Valid @RequestBody Oportunidade oportunidade) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(oportunidadeService.salvar(oportunidade));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Oportunidade> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody Oportunidade oportunidade) {
        return ResponseEntity.ok(oportunidadeService.atualizar(id, oportunidade));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        oportunidadeService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // --- RF005: Avaliação ---
    @PostMapping("/{id}/avaliacoes")
    public ResponseEntity<AvaliacaoOportunidade> avaliar(
            @PathVariable UUID id,
            @Valid @RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(oportunidadeService.avaliar(id, dto));
    }

    @GetMapping("/{id}/avaliacoes")
    public ResponseEntity<List<AvaliacaoOportunidade>> listarAvaliacoes(@PathVariable UUID id) {
        return ResponseEntity.ok(oportunidadeService.listarAvaliacoes(id));
    }

    @PostMapping("/{id}/enviar-aprovacao")
    public ResponseEntity<Oportunidade> enviarParaAprovacao(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowDecisionDTO dto) {
        return ResponseEntity.ok(oportunidadeService.enviarParaAprovacao(id, dto));
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Oportunidade> aprovar(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowDecisionDTO dto) {
        return ResponseEntity.ok(oportunidadeService.aprovar(id, dto));
    }

    @PostMapping("/{id}/reprovar")
    public ResponseEntity<Oportunidade> reprovar(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowDecisionDTO dto) {
        return ResponseEntity.ok(oportunidadeService.reprovar(id, dto));
    }
}