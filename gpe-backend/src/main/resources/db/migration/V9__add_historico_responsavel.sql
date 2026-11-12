ALTER TABLE tb_historico_parceiro
    ADD COLUMN responsavel_matricula VARCHAR(50);

UPDATE tb_historico_parceiro
SET responsavel_matricula = 'SISTEMA'
WHERE responsavel_matricula IS NULL;

ALTER TABLE tb_historico_parceiro
    ALTER COLUMN responsavel_matricula SET NOT NULL;
