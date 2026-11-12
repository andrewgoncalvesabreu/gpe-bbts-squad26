ALTER TABLE tb_documento
    ADD COLUMN parceria_id UUID;

ALTER TABLE tb_documento
    ADD CONSTRAINT fk_documento_parceria
        FOREIGN KEY (parceria_id) REFERENCES tb_parceria(id);

CREATE INDEX idx_documento_parceria ON tb_documento(parceria_id);
