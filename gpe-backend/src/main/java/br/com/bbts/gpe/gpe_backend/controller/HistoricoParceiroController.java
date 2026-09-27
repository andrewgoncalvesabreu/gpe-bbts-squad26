package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.HistoricoParceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.service.HistoricoParceiroService;
import br.com.bbts.gpe.gpe_backend.service.ParceiroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parceiros/{parceiroId}/historico")
public class HistoricoParceiroController {

    private final HistoricoParceiroService historicoService;
    private final ParceiroService parceiroService;

    public HistoricoParceiroController(
            HistoricoParceiroService historicoService,
            ParceiroService parceiroService) {

        this.historicoService = historicoService;
        this.parceiroService = parceiroService;
    }

    @GetMapping
    public ResponseEntity<List<HistoricoParceiro>> listarHistorico(
            @PathVariable UUID parceiroId) {

        parceiroService.buscarPorId(parceiroId);

        return ResponseEntity.ok(
                historicoService.listarPorParceiro(parceiroId)
        );
    }

    @PostMapping
    public ResponseEntity<HistoricoParceiro> adicionarHistorico(
            @PathVariable UUID parceiroId,
            @RequestBody HistoricoParceiro historico) {

        Parceiro parceiro = parceiroService.buscarPorId(parceiroId);

        historico.setParceiro(parceiro);

        HistoricoParceiro novoHistorico =
                historicoService.salvar(historico);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(novoHistorico);
    }
}