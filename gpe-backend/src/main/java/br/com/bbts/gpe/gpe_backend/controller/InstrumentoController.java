package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.ProrrogacaoInstrumento;
import br.com.bbts.gpe.gpe_backend.dto.ProrrogacaoInstrumentoRequestDTO;
import br.com.bbts.gpe.gpe_backend.dto.InstrumentoAlertaDTO;
import br.com.bbts.gpe.gpe_backend.service.InstrumentoService;
import br.com.bbts.gpe.gpe_backend.service.InstrumentoAlertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instrumentos")
public class InstrumentoController {

    private final InstrumentoService instrumentoService;
    private final InstrumentoAlertaService alertaService;

    public InstrumentoController(
            InstrumentoService instrumentoService,
            InstrumentoAlertaService alertaService) {
        this.instrumentoService = instrumentoService;
        this.alertaService = alertaService;
    }

    @GetMapping
    public ResponseEntity<List<InstrumentoJuridico>> listar(
            @RequestParam(required = false) UUID parceiroId) {
        return ResponseEntity.ok(instrumentoService.listar(parceiroId));
    }

    @GetMapping("/alertas/vencimento")
    public ResponseEntity<List<InstrumentoAlertaDTO>> listarAlertasVencimento() {
        return ResponseEntity.ok(alertaService.listarProximosVencimentos());
    }

    @PostMapping
    public ResponseEntity<InstrumentoJuridico> criar(
            @Valid @RequestBody InstrumentoJuridico instrumento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrumentoService.salvar(instrumento));
    }

    @GetMapping("/{id}/prorrogacoes")
    public ResponseEntity<List<ProrrogacaoInstrumento>> listarProrrogacoes(@PathVariable UUID id) {
        return ResponseEntity.ok(instrumentoService.listarProrrogacoes(id));
    }

    @PostMapping("/{id}/prorrogacoes")
    public ResponseEntity<ProrrogacaoInstrumento> prorrogar(
            @PathVariable UUID id,
            @Valid @RequestBody ProrrogacaoInstrumentoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrumentoService.prorrogar(id, request));
    }
}
