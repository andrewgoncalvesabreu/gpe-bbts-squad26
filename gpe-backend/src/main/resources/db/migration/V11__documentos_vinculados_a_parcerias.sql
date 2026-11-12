ALTER TABLE tb_documento
    ADD COLUMN versao_atual INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN status_assinatura VARCHAR(40) NOT NULL DEFAULT 'NAO_ENVIADO',
    ADD COLUMN parceria_id UUID;

UPDATE tb_documento documento
SET versao_atual = COALESCE((
    SELECT MAX(versao.numero_versao)
    FROM tb_documento_versao versao
    WHERE versao.documento_id = documento.id
), 1);

ALTER TABLE tb_documento
    ADD CONSTRAINT fk_documento_parceria
        FOREIGN KEY (parceria_id) REFERENCES tb_parceria(id);

CREATE INDEX idx_documento_parceria ON tb_documento(parceria_id);

ALTER TABLE tb_documento_versao
    ADD COLUMN responsavel_matricula VARCHAR(50);

CREATE TABLE tb_assinatura_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    documento_versao_id UUID NOT NULL,
    provedor VARCHAR(100) NOT NULL,
    status VARCHAR(40) NOT NULL,
    protocolo VARCHAR(200),
    detalhe TEXT,
    responsavel_matricula VARCHAR(50) NOT NULL,
    data_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assinatura_documento
        FOREIGN KEY (documento_id) REFERENCES tb_documento(id) ON DELETE CASCADE,
    CONSTRAINT fk_assinatura_versao
        FOREIGN KEY (documento_versao_id) REFERENCES tb_documento_versao(id)
);

CREATE INDEX idx_assinatura_documento_data
    ON tb_assinatura_documento(documento_id, data_evento DESC);
