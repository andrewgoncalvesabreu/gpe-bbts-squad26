package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.Auditoria;
import br.com.bbts.gpe.gpe_backend.domain.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Auditoria registrar(
            String operacao,
            String entidade,
            UUID entidadeId,
            Object conteudoAnterior,
            Object conteudoNovo,
            String ipOrigem) {
        Auditoria auditoria = Auditoria.builder()
                .usuario(usuarioAtual())
                .operacao(operacao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .dataHora(LocalDateTime.now())
                .conteudoAnterior(serializar(conteudoAnterior))
                .conteudoNovo(serializar(conteudoNovo))
                .ipOrigem(ipOrigem)
                .build();

        return auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> consultar(
            String usuario,
            String operacao,
            String entidade,
            LocalDateTime inicio,
            LocalDateTime fim) {
        return auditoriaRepository.consultar(usuario, operacao, entidade, inicio, fim);
    }

    private String usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "sistema";
        }
        return authentication.getName();
    }

    private String serializar(Object conteudo) {
        if (conteudo == null) {
            return null;
        }
        if (conteudo instanceof String texto) {
            return texto;
        }
        try {
            return objectMapper.writeValueAsString(conteudo);
        } catch (JacksonException exception) {
            return "{\"erro\":\"falha ao serializar snapshot de auditoria\"}";
        }
    }
}
