package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.service.ParceiroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parceiros")
public class ParceiroController {

    private final ParceiroService parceiroService;

    public ParceiroController(ParceiroService parceiroService) {
        this.parceiroService = parceiroService;
    }

    @GetMapping
    public ResponseEntity<List<Parceiro>> listarParceiros() {
        return ResponseEntity.ok(parceiroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parceiro> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(parceiroService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS', 'ANALISTA')")
    public ResponseEntity<Parceiro> criarParceiro(
        @Valid @RequestBody Parceiro parceiro,
        @RequestHeader("X-Responsavel-Matricula") String responsavelMatricula) {

        Parceiro novoParceiro = parceiroService.salvar(parceiro, responsavelMatricula);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(novoParceiro);
}

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS', 'ANALISTA')")
    public ResponseEntity<Parceiro> atualizarParceiro(
        @PathVariable UUID id,
        @Valid @RequestBody Parceiro parceiro,
        @RequestHeader("X-Responsavel-Matricula") String responsavelMatricula) {

        return ResponseEntity.ok(
            parceiroService.atualizar(id, parceiro, responsavelMatricula)
    );
}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS')")
    public ResponseEntity<Void> excluirParceiro(
            @PathVariable UUID id,
            @RequestHeader("X-Responsavel-Matricula") String responsavelMatricula) {

        parceiroService.excluir(id, responsavelMatricula);

        return ResponseEntity.noContent().build();
    }
}
