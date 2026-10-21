package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.dto.InstrumentoAlertaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoInstrumentoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoInstrumentoService.class);

    private final InstrumentoAlertaService alertaService;

    public NotificacaoInstrumentoService(InstrumentoAlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Scheduled(cron = "${app.alertas.instrumentos.cron:0 0 8 * * *}")
    public void notificarVencimentosProximos() {
        List<InstrumentoAlertaDTO> alertas = alertaService.listarProximosVencimentos();
        for (InstrumentoAlertaDTO alerta : alertas) {
            LOGGER.warn(
                    "Instrumento jurídico próximo do vencimento: '{}' (parceiro: '{}'), vence em {} ({} dias)",
                    alerta.instrumentoNome(),
                    alerta.parceiroNome(),
                    alerta.dataVencimento(),
                    alerta.diasAteVencimento());
        }
    }
}
