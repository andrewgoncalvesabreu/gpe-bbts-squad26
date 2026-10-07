package br.com.bbts.gpe.gpe_backend.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_avaliacao_oportunidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoOportunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oportunidade_id", nullable = false)
    private Oportunidade oportunidade;

    @Min(1) @Max(5)
    @Column(name = "alinhamento_estrategico", nullable = false)
    private Integer alinhamentoEstrategico;

    @Min(1) @Max(5)
    @Column(name = "potencial_inovacao", nullable = false)
    private Integer potencialInovacao;

    @Min(1) @Max(5)
    @Column(name = "retorno_financeiro", nullable = false)
    private Integer retornoFinanceiro;

    @Min(1) @Max(5)
    @Column(name = "impacto_institucional", nullable = false)
    private Integer impactoInstitucional;

    @Min(1) @Max(5)
    @Column(name = "viabilidade_tecnica", nullable = false)
    private Integer viabilidadeTecnica;

    @Column(name = "parecer_tecnico", columnDefinition = "TEXT")
    private String parecerTecnico;

    @NotBlank
    @Column(name = "avaliador_matricula", nullable = false, length = 50)
    private String avaliadorMatricula;

    @CreationTimestamp
    @Column(name = "data_avaliacao", nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;
}