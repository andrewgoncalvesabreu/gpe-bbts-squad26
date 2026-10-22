package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.Oportunidade;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.model.ProrrogacaoInstrumento;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.OportunidadeRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ProrrogacaoInstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.dto.ProrrogacaoInstrumentoRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@Service
public class InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final ProrrogacaoInstrumentoRepository prorrogacaoRepository;
    private final ParceiroService parceiroService;

    public InstrumentoService(
            InstrumentoRepository instrumentoRepository,
            OportunidadeRepository oportunidadeRepository,
            ProrrogacaoInstrumentoRepository prorrogacaoRepository,
            ParceiroService parceiroService) {
        this.instrumentoRepository = instrumentoRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.prorrogacaoRepository = prorrogacaoRepository;
        this.parceiroService = parceiroService;
    }

    @Transactional(readOnly = true)
    public List<InstrumentoJuridico> listar(UUID parceiroId) {
        if (parceiroId == null) {
            return instrumentoRepository.findAll();
        }
        parceiroService.buscarPorId(parceiroId);
        return instrumentoRepository.findByParceiroIdOrderByDataCadastroDesc(parceiroId);
    }

    @Transactional
    public InstrumentoJuridico salvar(InstrumentoJuridico instrumento) {
        validarVigencia(instrumento.getDataInicioVigencia(), instrumento.getDataFimVigencia());

        if (instrumento.getParceiro() == null || instrumento.getParceiro().getId() == null) {
            throw new BusinessException("Informe o parceiro do instrumento (parceiro.id)");
        }

        Parceiro parceiro = parceiroService.buscarPorId(instrumento.getParceiro().getId());
        instrumento.setParceiro(parceiro);
        instrumento.setId(null);

        if (instrumento.getOportunidade() != null) {
            UUID oportunidadeId = instrumento.getOportunidade().getId();
            if (oportunidadeId == null) {
                throw new BusinessException("Informe o identificador da oportunidade vinculada");
            }
            Oportunidade oportunidade = oportunidadeRepository.findById(oportunidadeId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Oportunidade não encontrada: " + oportunidadeId));
            if (!parceiro.getId().equals(oportunidade.getParceiro().getId())) {
                throw new BusinessException("A oportunidade deve pertencer ao parceiro selecionado");
            }
            instrumento.setOportunidade(oportunidade);
        }

        return instrumentoRepository.save(instrumento);
    }

    @Transactional(readOnly = true)
    public List<ProrrogacaoInstrumento> listarProrrogacoes(UUID instrumentoId) {
        buscarPorId(instrumentoId);
        return prorrogacaoRepository.findByInstrumentoIdOrderByDataProrrogacaoDesc(instrumentoId);
    }

    @Transactional
    public ProrrogacaoInstrumento prorrogar(
            UUID instrumentoId,
            ProrrogacaoInstrumentoRequestDTO request) {
        InstrumentoJuridico instrumento = buscarPorId(instrumentoId);
        LocalDate novaDataFim = request.getNovaDataFim();
        if (instrumento.getDataFimVigencia() == null) {
            throw new BusinessException("O instrumento não possui data de término vigente");
        }
        validarVigencia(instrumento.getDataInicioVigencia(), novaDataFim);
        if (!novaDataFim.isAfter(instrumento.getDataFimVigencia())) {
            throw new BusinessException("A nova data de término deve ser posterior à vigência atual");
        }

        ProrrogacaoInstrumento prorrogacao = ProrrogacaoInstrumento.builder()
                .instrumento(instrumento)
                .dataFimAnterior(instrumento.getDataFimVigencia())
                .dataFimNova(novaDataFim)
                .justificativa(request.getJustificativa())
                .build();
        instrumento.setDataFimVigencia(novaDataFim);
        instrumentoRepository.save(instrumento);
        return prorrogacaoRepository.save(prorrogacao);
    }

    private InstrumentoJuridico buscarPorId(UUID id) {
        return instrumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrumento não encontrado: " + id));
    }

    private void validarVigencia(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new BusinessException("Informe as datas de início e término da vigência");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new BusinessException("Data de término não pode ser anterior à data de início");
        }
    }
}
