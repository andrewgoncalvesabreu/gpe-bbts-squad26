package br.com.bbts.gpe.gpe_backend.domain.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_prorrogacao_instrumento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProrrogacaoInstrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    @JsonIgnore
    private InstrumentoJuridico instrumento;

    @Column(name = "data_fim_anterior", nullable = false)
    private LocalDate dataFimAnterior;

    @Column(name = "data_fim_nova", nullable = false)
    private LocalDate dataFimNova;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @CreationTimestamp
    @Column(name = "data_prorrogacao", nullable = false, updatable = false)
    private LocalDateTime dataProrrogacao;
}
