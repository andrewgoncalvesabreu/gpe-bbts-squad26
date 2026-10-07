package br.com.bbts.gpe.gpe_backend.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_oportunidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oportunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotNull(message = "Parceiro é obrigatório")
    @ManyToOne
    @JoinColumn(name = "parceiro_id", nullable = false)
    private Parceiro parceiro;

    @NotBlank(message = "Origem é obrigatória")
    @Size(max = 50, message = "Origem deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String origem;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(columnDefinition = "TEXT")
    private String descricao;

    @NotBlank(message = "Objetivos são obrigatórios")
    @Column(columnDefinition = "TEXT")
    private String objetivos;

    @NotBlank(message = "Justificativa é obrigatória")
    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @NotBlank(message = "Benefícios esperados são obrigatórios")
    @Column(name = "beneficios_esperados", columnDefinition = "TEXT")
    private String beneficiosEsperados;

    @Column(name = "problema_demandado", columnDefinition = "TEXT")
    private String problemaDemandado;

    @Column(name = "solucao_proposta", columnDefinition = "TEXT")
    private String solucaoProposta;

    @PositiveOrZero(message = "Estimativa financeira não pode ser negativa")
    @Digits(integer = 13, fraction = 2, message = "Estimativa financeira inválida")
    @Column(name = "estimativa_financeira", precision = 15, scale = 2)
    private BigDecimal estimativaFinanceira;

    @Size(max = 50, message = "Status deve ter no máximo 50 caracteres")
    @Column(name = "status_workflow", nullable = false, length = 50)
    private String statusWorkflow;

    @NotBlank(message = "Área demandante é obrigatória")
    @Size(max = 100, message = "Área demandante deve ter no máximo 100 caracteres")
    @Column(name = "area_demandante", nullable = false, length = 100)
    private String areaDemandante;

    @NotBlank(message = "Responsável é obrigatório")
    @Size(max = 50, message = "Responsável deve ter no máximo 50 caracteres")
    @Column(name = "responsavel_matricula", nullable = false, length = 50)
    private String responsavelMatricula;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}