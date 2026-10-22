CREATE TABLE tb_instrumento_juridico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    numero_instrumento VARCHAR(100),
    descricao TEXT,
    parceiro_id UUID NOT NULL,
    oportunidade_id UUID,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_instrumento_parceiro
        FOREIGN KEY (parceiro_id) REFERENCES tb_parceiro(id),
    CONSTRAINT fk_instrumento_oportunidade
        FOREIGN KEY (oportunidade_id) REFERENCES tb_oportunidade(id)
);

CREATE INDEX idx_instrumento_parceiro ON tb_instrumento_juridico(parceiro_id);
CREATE INDEX idx_instrumento_oportunidade ON tb_instrumento_juridico(oportunidade_id);
