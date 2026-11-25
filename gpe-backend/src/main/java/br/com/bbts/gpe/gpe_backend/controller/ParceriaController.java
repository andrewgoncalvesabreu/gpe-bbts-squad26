package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.dto.ParceriaDTO;
import br.com.bbts.gpe.gpe_backend.dto.ParceriaRequestDTO;
import br.com.bbts.gpe.gpe_backend.service.ParceriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parcerias")
public class ParceriaController {
    private final ParceriaService parceriaService;

    public ParceriaController(ParceriaService parceriaService) {
        this.parceriaService = parceriaService;
    }

    @GetMapping
    public ResponseEntity<List<ParceriaDTO>> listar(
            @RequestParam(name = "parceiroId", required = false) UUID parceiroId) {
        return ResponseEntity.ok(parceriaService.listar(parceiroId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR_PARCEIRAS', 'ANALISTA')")
    public ResponseEntity<ParceriaDTO> criar(@Valid @RequestBody ParceriaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parceriaService.criar(request));
    }
}
