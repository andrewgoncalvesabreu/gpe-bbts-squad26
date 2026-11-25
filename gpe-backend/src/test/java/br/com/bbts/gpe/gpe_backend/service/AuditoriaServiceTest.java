package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.Auditoria;
import br.com.bbts.gpe.gpe_backend.domain.repository.AuditoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraUsuarioEConteudoDaOperacao() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"nome\":\"novo\"}");
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID entidadeId = UUID.randomUUID();
        auditoriaService.registrar("UPDATE", "Parceiro", entidadeId, null, new Object(), "127.0.0.1");

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        Auditoria auditoria = captor.getValue();

        assertEquals("admin", auditoria.getUsuario());
        assertEquals("UPDATE", auditoria.getOperacao());
        assertEquals("Parceiro", auditoria.getEntidade());
        assertEquals(entidadeId, auditoria.getEntidadeId());
        assertEquals("{\"nome\":\"novo\"}", auditoria.getConteudoNovo());
        assertEquals("127.0.0.1", auditoria.getIpOrigem());
        assertNotNull(auditoria.getDataHora());
    }
}
