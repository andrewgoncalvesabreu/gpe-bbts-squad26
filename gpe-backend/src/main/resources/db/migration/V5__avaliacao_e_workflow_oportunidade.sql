CREATE TABLE tb_avaliacao_oportunidade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    oportunidade_id UUID NOT NULL,
    alinhamento_estrategico INT NOT NULL CHECK (alinhamento_estrategico BETWEEN 1 AND 5),
    potencial_inovacao INT NOT NULL CHECK (potencial_inovacao BETWEEN 1 AND 5),
    retorno_financeiro INT NOT NULL CHECK (retorno_financeiro BETWEEN 1 AND 5),
    impacto_institucional INT NOT NULL CHECK (impacto_institucional BETWEEN 1 AND 5),
    viabilidade_tecnica INT NOT NULL CHECK (viabilidade_tecnica BETWEEN 1 AND 5),
    parecer_tecnico TEXT,
    avaliador_matricula VARCHAR(50) NOT NULL,
    data_avaliacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avaliacao_oportunidade FOREIGN KEY (oportunidade_id) REFERENCES tb_oportunidade(id) ON DELETE CASCADE
);