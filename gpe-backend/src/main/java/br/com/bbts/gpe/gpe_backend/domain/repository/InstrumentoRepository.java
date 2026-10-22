package br.com.bbts.gpe.gpe_backend.domain.repository;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface InstrumentoRepository extends JpaRepository<InstrumentoJuridico, UUID> {
    List<InstrumentoJuridico> findByParceiroIdOrderByDataCadastroDesc(UUID parceiroId);

    List<InstrumentoJuridico> findByDataFimVigenciaGreaterThanEqualAndDataFimVigenciaLessThanEqualOrderByDataFimVigenciaAsc(
            LocalDate inicio,
            LocalDate fim);
}
