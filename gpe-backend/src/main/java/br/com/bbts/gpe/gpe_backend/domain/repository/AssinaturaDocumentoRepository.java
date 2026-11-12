package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.AssinaturaDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssinaturaDocumentoRepository extends JpaRepository<AssinaturaDocumento, UUID> {
    List<AssinaturaDocumento> findByDocumentoIdOrderByDataEventoDesc(UUID documentoId);
}
