ALTER TABLE tb_documento
    ADD COLUMN IF NOT EXISTS oportunidade_id UUID,
    ADD COLUMN IF NOT EXISTS instrumento_id UUID,
    ADD COLUMN IF NOT EXISTS parceria_id UUID,
    ADD COLUMN IF NOT EXISTS data_atualizacao TIMESTAMP,
    ADD COLUMN IF NOT EXISTS ativo BOOLEAN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'chave_armazenamento'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'caminho_arquivo'
    ) THEN
        ALTER TABLE tb_documento_versao RENAME COLUMN chave_armazenamento TO caminho_arquivo;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'tipo_mime'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'content_type'
    ) THEN
        ALTER TABLE tb_documento_versao RENAME COLUMN tipo_mime TO content_type;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'sha256'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'hash_sha256'
    ) THEN
        ALTER TABLE tb_documento_versao RENAME COLUMN sha256 TO hash_sha256;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'data_upload'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tb_documento_versao'
          AND column_name = 'data_envio'
    ) THEN
        ALTER TABLE tb_documento_versao RENAME COLUMN data_upload TO data_envio;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'tb_documento'::regclass
          AND conname = 'fk_documento_oportunidade'
    ) THEN
        ALTER TABLE tb_documento
            ADD CONSTRAINT fk_documento_oportunidade
            FOREIGN KEY (oportunidade_id) REFERENCES tb_oportunidade(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'tb_documento'::regclass
          AND conname = 'fk_documento_instrumento'
    ) THEN
        ALTER TABLE tb_documento
            ADD CONSTRAINT fk_documento_instrumento
            FOREIGN KEY (instrumento_id) REFERENCES tb_instrumento_juridico(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'tb_documento'::regclass
          AND conname = 'fk_documento_parceria'
    ) THEN
        ALTER TABLE tb_documento
            ADD CONSTRAINT fk_documento_parceria
            FOREIGN KEY (parceria_id) REFERENCES tb_parceria(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'tb_documento'::regclass
          AND conname = 'ck_documento_vinculo'
    ) THEN
        ALTER TABLE tb_documento
            ADD CONSTRAINT ck_documento_vinculo
            CHECK (
                parceiro_id IS NOT NULL
                OR oportunidade_id IS NOT NULL
                OR instrumento_id IS NOT NULL
                OR parceria_id IS NOT NULL
            );
    END IF;
END $$;

UPDATE tb_documento
SET data_atualizacao = COALESCE(data_atualizacao, data_criacao, CURRENT_TIMESTAMP),
    ativo = COALESCE(ativo, TRUE);

ALTER TABLE tb_documento
    ALTER COLUMN parceiro_id DROP NOT NULL,
    ALTER COLUMN data_atualizacao SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN data_atualizacao SET NOT NULL,
    ALTER COLUMN ativo SET DEFAULT TRUE,
    ALTER COLUMN ativo SET NOT NULL;

ALTER TABLE tb_documento_versao
    ADD COLUMN IF NOT EXISTS caminho_arquivo VARCHAR(500),
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(150),
    ADD COLUMN IF NOT EXISTS hash_sha256 VARCHAR(64),
    ADD COLUMN IF NOT EXISTS data_envio TIMESTAMP,
    ADD COLUMN IF NOT EXISTS responsavel_matricula VARCHAR(50);

UPDATE tb_documento_versao
SET data_envio = COALESCE(data_envio, CURRENT_TIMESTAMP);

ALTER TABLE tb_documento_versao
    ALTER COLUMN caminho_arquivo TYPE VARCHAR(500),
    ALTER COLUMN content_type TYPE VARCHAR(150),
    ALTER COLUMN hash_sha256 TYPE VARCHAR(64),
    ALTER COLUMN caminho_arquivo SET NOT NULL,
    ALTER COLUMN content_type SET NOT NULL,
    ALTER COLUMN hash_sha256 SET NOT NULL,
    ALTER COLUMN data_envio SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN data_envio SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_documento_oportunidade ON tb_documento(oportunidade_id);
CREATE INDEX IF NOT EXISTS idx_documento_instrumento ON tb_documento(instrumento_id);
CREATE INDEX IF NOT EXISTS idx_documento_parceria ON tb_documento(parceria_id);
CREATE INDEX IF NOT EXISTS idx_versao_documento ON tb_documento_versao(documento_id);
