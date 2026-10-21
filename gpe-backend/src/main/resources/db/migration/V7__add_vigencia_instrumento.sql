ALTER TABLE tb_instrumento_juridico
    ADD COLUMN data_inicio_vigencia DATE,
    ADD COLUMN data_fim_vigencia DATE,
    ADD CONSTRAINT ck_instrumento_periodo_vigencia
        CHECK (
            data_inicio_vigencia IS NULL
            OR data_fim_vigencia IS NULL
            OR data_fim_vigencia >= data_inicio_vigencia
        );

CREATE TABLE tb_prorrogacao_instrumento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrumento_id UUID NOT NULL,
    data_fim_anterior DATE NOT NULL,
    data_fim_nova DATE NOT NULL,
    justificativa TEXT,
    data_prorrogacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prorrogacao_instrumento
        FOREIGN KEY (instrumento_id) REFERENCES tb_instrumento_juridico(id),
    CONSTRAINT ck_prorrogacao_data_fim
        CHECK (data_fim_nova > data_fim_anterior)
);

CREATE INDEX idx_prorrogacao_instrumento
    ON tb_prorrogacao_instrumento(instrumento_id);
