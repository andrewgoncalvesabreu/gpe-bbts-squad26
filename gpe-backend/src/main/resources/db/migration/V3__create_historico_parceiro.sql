CREATE TABLE tb_historico_parceiro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parceiro_id UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descricao TEXT NOT NULL,
    data_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historico_parceiro
        FOREIGN KEY (parceiro_id)
        REFERENCES tb_parceiro(id)
);

CREATE INDEX idx_historico_parceiro
    ON tb_historico_parceiro(parceiro_id);