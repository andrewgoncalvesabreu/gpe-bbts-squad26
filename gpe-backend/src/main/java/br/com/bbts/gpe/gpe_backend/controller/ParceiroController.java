package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.service.ParceiroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<Parceiro> criarParceiro(@RequestBody Parceiro parceiro) {
        Parceiro novoParceiro = parceiroService.salvar(parceiro);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoParceiro);
    }
}