package br.com.bbts.gpe.gpe_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/parceiros")
public class ParceiroController {

    @GetMapping
    public List<Map<String, String>> listarParceiros() {
        return List.of(
            Map.of("id", "1", "razaoSocial", "Empresa Teste BBTS", "cnpj", "00.000.000/0001-91", "status", "HOMOLOGADO")
        );
    }
}