package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<Documento, UUID> {
    List<Documento> findByAtivoTrueOrderByDataAtualizacaoDesc();
    List<Documento> findByParceiroIdAndAtivoTrueOrderByDataAtualizacaoDesc(UUID parceiroId);
    List<Documento> findByOportunidadeIdAndAtivoTrueOrderByDataAtualizacaoDesc(UUID oportunidadeId);
    List<Documento> findByInstrumentoIdAndAtivoTrueOrderByDataAtualizacaoDesc(UUID instrumentoId);
}