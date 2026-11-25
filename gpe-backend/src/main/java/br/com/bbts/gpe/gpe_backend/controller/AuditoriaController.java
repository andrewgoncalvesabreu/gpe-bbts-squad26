package br.com.bbts.gpe.gpe_backend.controller;

import br.com.bbts.gpe.gpe_backend.domain.model.Auditoria;
import br.com.bbts.gpe.gpe_backend.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auditorias")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AuditoriaController {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Auditoria>> consultar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String operacao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(auditoriaService.consultar(usuario, operacao, entidade, inicio, fim));
    }
}
