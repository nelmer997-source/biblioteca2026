-- Consulta segura em protecao_autenticacao (sem expor chave_login/hash ou credenciais)
SELECT
    id,
    falhas_consecutivas,
    janela_iniciada_em,
    bloqueada_ate
FROM protecao_autenticacao
ORDER BY id DESC;

-- Consulta segura em evento_seguranca (projeção explícita sem identificador_hash)
SELECT
    id,
    tipo,
    ocorrido_em,
    ator_conta_id,
    alvo_conta_id,
    endereco_origem
FROM evento_seguranca
ORDER BY ocorrido_em DESC, id DESC;
