package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.dto.PlanoTrabalhoDTO;
import br.com.bbts.gpe.gpe_backend.dto.PlanoTrabalhoRequestDTO;
import br.com.bbts.gpe.gpe_backend.service.PlanoTrabalhoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/plano-trabalho")
public class PlanoTrabalhoController {
    private final PlanoTrabalhoService planoTrabalhoService;

    public PlanoTrabalhoController(PlanoTrabalhoService planoTrabalhoService) {
        this.planoTrabalhoService = planoTrabalhoService;
    }

    @GetMapping
    public ResponseEntity<PlanoTrabalhoDTO> buscar(@PathVariable UUID projetoId) {
        return ResponseEntity.ok(planoTrabalhoService.buscar(projetoId));
    }

    @PostMapping
    public ResponseEntity<PlanoTrabalhoDTO> criar(
            @PathVariable UUID projetoId,
            @Valid @RequestBody PlanoTrabalhoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planoTrabalhoService.criar(projetoId, request));
    }

    @PutMapping
    public ResponseEntity<PlanoTrabalhoDTO> atualizar(
            @PathVariable UUID projetoId,
            @Valid @RequestBody PlanoTrabalhoRequestDTO request) {
        return ResponseEntity.ok(planoTrabalhoService.atualizar(projetoId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> excluir(@PathVariable UUID projetoId) {
        planoTrabalhoService.excluir(projetoId);
        return ResponseEntity.noContent().build();
    }
}
