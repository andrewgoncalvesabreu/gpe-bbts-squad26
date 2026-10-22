package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.dto.InstrumentoAlertaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InstrumentoAlertaService {
    public static final int DIAS_ANTECEDENCIA = 30;

    private final InstrumentoRepository instrumentoRepository;

    public InstrumentoAlertaService(InstrumentoRepository instrumentoRepository) {
        this.instrumentoRepository = instrumentoRepository;
    }

    @Transactional(readOnly = true)
    public List<InstrumentoAlertaDTO> listarProximosVencimentos() {
        return listarProximosVencimentos(LocalDate.now());
    }

    List<InstrumentoAlertaDTO> listarProximosVencimentos(LocalDate hoje) {
        LocalDate limite = hoje.plusDays(DIAS_ANTECEDENCIA);
        return instrumentoRepository
                .findByDataFimVigenciaGreaterThanEqualAndDataFimVigenciaLessThanEqualOrderByDataFimVigenciaAsc(
                        hoje, limite)
                .stream()
                .filter(instrumento -> !instrumento.getDataFimVigencia().isBefore(hoje)
                        && !instrumento.getDataFimVigencia().isAfter(limite))
                .map(instrumento -> toAlerta(instrumento, hoje))
                .toList();
    }

    private InstrumentoAlertaDTO toAlerta(InstrumentoJuridico instrumento, LocalDate hoje) {
        return new InstrumentoAlertaDTO(
                instrumento.getId(),
                instrumento.getNome(),
                instrumento.getParceiro().getRazaoSocial(),
                instrumento.getDataFimVigencia(),
                ChronoUnit.DAYS.between(hoje, instrumento.getDataFimVigencia()));
    }
}
