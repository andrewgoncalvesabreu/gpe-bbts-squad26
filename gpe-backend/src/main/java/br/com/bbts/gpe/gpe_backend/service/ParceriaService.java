package br.com.bbts.gpe.gpe_backend.service;

import br.com.bbts.gpe.gpe_backend.domain.model.InstrumentoJuridico;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceria;
import br.com.bbts.gpe.gpe_backend.domain.model.Parceiro;
import br.com.bbts.gpe.gpe_backend.domain.repository.InstrumentoRepository;
import br.com.bbts.gpe.gpe_backend.domain.repository.ParceriaRepository;
import br.com.bbts.gpe.gpe_backend.dto.ParceriaDTO;
import br.com.bbts.gpe.gpe_backend.dto.ParceriaRequestDTO;
import br.com.bbts.gpe.gpe_backend.exception.BusinessException;
import br.com.bbts.gpe.gpe_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ParceriaService {
    private final ParceriaRepository parceriaRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final ParceiroService parceiroService;

    public ParceriaService(
            ParceriaRepository parceriaRepository,
            InstrumentoRepository instrumentoRepository,
            ParceiroService parceiroService) {
        this.parceriaRepository = parceriaRepository;
        this.instrumentoRepository = instrumentoRepository;
        this.parceiroService = parceiroService;
    }

    @Transactional(readOnly = true)
    public List<ParceriaDTO> listar(UUID parceiroId) {
        List<Parceria> parcerias = parceiroId == null
                ? parceriaRepository.findAllByOrderByDataCriacaoDesc()
                : parceriaRepository.findByParceiroIdOrderByDataCriacaoDesc(parceiroId);
        return parcerias
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ParceriaDTO criar(ParceriaRequestDTO request) {
        Parceiro parceiro = parceiroService.buscarPorId(request.parceiroId());
        if ("INATIVO".equalsIgnoreCase(parceiro.getStatus())) {
            throw new BusinessException("Não é possível criar parceria com parceiro inativo");
        }
        InstrumentoJuridico instrumento = instrumentoRepository.findById(request.instrumentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instrumento não encontrado: " + request.instrumentoId()));
        if (!parceiro.getId().equals(instrumento.getParceiro().getId())) {
            throw new BusinessException("O instrumento jurídico não pertence ao parceiro selecionado");
        }
        if (parceriaRepository.existsByInstrumentoId(instrumento.getId())) {
            throw new BusinessException("Este instrumento já está vinculado a uma parceria");
        }
        Parceria parceria = parceriaRepository.save(Parceria.builder()
                .nome(request.nome().trim())
                .parceiro(parceiro)
                .instrumento(instrumento)
                .status("ATIVA")
                .build());
        return toDTO(parceria);
    }

    private ParceriaDTO toDTO(Parceria parceria) {
        return new ParceriaDTO(parceria.getId(), parceria.getNome(),
                parceria.getParceiro().getId(), parceria.getParceiro().getRazaoSocial(),
                parceria.getInstrumento().getId(), parceria.getInstrumento().getNome(),
                parceria.getStatus(), parceria.getDataCriacao());
    }
}
