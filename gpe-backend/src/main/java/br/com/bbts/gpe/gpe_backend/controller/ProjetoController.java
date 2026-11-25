package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.dto.ProjetoDTO;
import br.com.bbts.gpe.gpe_backend.dto.ProjetoRequestDTO;
import br.com.bbts.gpe.gpe_backend.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projetos")
public class ProjetoController {
    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @GetMapping
    public ResponseEntity<List<ProjetoDTO>> listar(@RequestParam(required = false) UUID parceriaId) {
        return ResponseEntity.ok(projetoService.listar(parceriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(projetoService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS', 'ANALISTA')")
    public ResponseEntity<ProjetoDTO> criar(@Valid @RequestBody ProjetoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS', 'ANALISTA')")
    public ResponseEntity<ProjetoDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProjetoRequestDTO request) {
        return ResponseEntity.ok(projetoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        projetoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
