package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.service.ParceiroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Parceiro> criarParceiro(
        @Valid @RequestBody Parceiro parceiro) {

        Parceiro novoParceiro = parceiroService.salvar(parceiro);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(novoParceiro);
}

    @PutMapping("/{id}")
    public ResponseEntity<Parceiro> atualizarParceiro(
        @PathVariable UUID id,
        @Valid @RequestBody Parceiro parceiro) {

        return ResponseEntity.ok(
            parceiroService.atualizar(id, parceiro)
    );
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirParceiro(
            @PathVariable UUID id) {

        parceiroService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}