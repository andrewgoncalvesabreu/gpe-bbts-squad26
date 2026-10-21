package br.com.bbts.gpe.gpe_backend.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_instrumento_juridico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentoJuridico {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Nome do instrumento é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nome;

    @NotNull(message = "Tipo de instrumento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoInstrumento tipo;

    @Size(max = 100, message = "Número deve ter no máximo 100 caracteres")
    @Column(name = "numero_instrumento", length = 100)
    private String numeroInstrumento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de início da vigência é obrigatória")
    @Column(name = "data_inicio_vigencia")
    private LocalDate dataInicioVigencia;

    @NotNull(message = "Data de término da vigência é obrigatória")
    @Column(name = "data_fim_vigencia")
    private LocalDate dataFimVigencia;

    @NotNull(message = "Parceiro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id", nullable = false)
    private Parceiro parceiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oportunidade_id")
    private Oportunidade oportunidade;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @AssertTrue(message = "Data de término não pode ser anterior à data de início")
    @JsonIgnore
    public boolean isPeriodoVigenciaValido() {
        return dataInicioVigencia == null || dataFimVigencia == null
                || !dataFimVigencia.isBefore(dataInicioVigencia);
    }
}
