package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.DocumentoVersao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoVersaoRepository extends JpaRepository<DocumentoVersao, UUID> {
    List<DocumentoVersao> findByDocumentoIdOrderByNumeroVersaoDesc(UUID documentoId);
    Optional<DocumentoVersao> findTopByDocumentoIdOrderByNumeroVersaoDesc(UUID documentoId);
    Optional<DocumentoVersao> findByDocumentoIdAndNumeroVersao(UUID documentoId, Integer numeroVersao);
}