# Evidencias da Aula 17 — Auditoria e Protecao da Autenticacao (`biblioteca2026`)

## 1. Linha do Tempo — 5 Falhas Consecutivas e Eventos Gerados

| Etapa | Acao / Requisicao | HTTP | Estado em `protecao_autenticacao` | Eventos em `evento_seguranca` |
| :--- | :--- | :---: | :--- | :--- |
| 1 | `POST /api/autenticacao/login` (senha incorreta #1) | 401 | `falhas_consecutivas = 1`, `janela_iniciada_em = T0`, `bloqueada_ate = null` | `LOGIN_FALHA` |
| 2 | `POST /api/autenticacao/login` (senha incorreta #2) | 401 | `falhas_consecutivas = 2`, `bloqueada_ate = null` | `LOGIN_FALHA` |
| 3 | `POST /api/autenticacao/login` (senha incorreta #3) | 401 | `falhas_consecutivas = 3`, `bloqueada_ate = null` | `LOGIN_FALHA` |
| 4 | `POST /api/autenticacao/login` (senha incorreta #4) | 401 | `falhas_consecutivas = 4`, `bloqueada_ate = null` | `LOGIN_FALHA` |
| 5 | `POST /api/autenticacao/login` (senha incorreta #5 — atinge limite) | 401 | `falhas_consecutivas = 5`, `bloqueada_ate = T5 + lockDuration` | `LOGIN_FALHA` + `BLOQUEIO_TEMPORARIO_ATIVADO` |
| 6 | `POST /api/autenticacao/login` (senha **correta** durante bloqueio) | 401 | `falhas_consecutivas = 5`, bloqueio mantido | `LOGIN_RECUSADO_BLOQUEIO` |

## 2. Respostas HTTP Obtidas no Postman (6a Tentativa vs. Conta Inexistente)

- **6a tentativa com senha correta durante bloqueio temporario**: retorna `401 Unauthorized` com corpo generico (`status: 401`, `message: "Login ou senha inválidos"`), identico ao retorno de falha de credencial.
- **Conta existente vs. conta inexistente**: ambos retornam `401 Unauthorized` com a mesma mensagem generica (`"Login ou senha inválidos"`), evitando enumeracao de usuarios e sem gravar login em texto aberto nem `endereco_origem` invalido nos eventos.

## 3. Consulta SQL Sem Segredos

As consultas oficiais estao em `docs/aula-17/consultas_auditoria.sql`, projetando explicitamente apenas metadados de auditoria/bloqueio e omitindo `identificador_hash`, `senha_hash` e tokens.

## 4. Matriz Ameaca–Controle

| Ameaca | Controle Implementado | Evidencia de Teste | Risco Residual |
| :--- | :--- | :--- | :--- |
| Forca bruta em credenciais por login | Contador de falhas com janela (`failureWindow`), bloqueio temporario (`lockDuration`) e lock pessimista (`PESSIMISTIC_WRITE`) + upsert `ON CONFLICT DO NOTHING` | `ProtecaoAutenticacaoApiTest` e `ProtecaoAutenticacaoTest` | Baixo; mitiga tentativas rapidas por conta, exigindo ajuste de `maxFailedAttempts`, `failureWindow` e `lockDuration` conforme perfil da aplicacao |
| Enumeracao de contas via erro de login / vazamento em auditoria | Resposta `401` generica uniforme; `chaveLogin` em SHA-256 (`64` hex); `EventoSegurancaResponse` e SQL sem expor `identificadorHash` | `ProtecaoAutenticacaoApiTest.deveManterRespostaGenericaParaContaInexistenteSemGravarLoginOriginal` | Baixo; temporizacao do `PasswordEncoder` pode variar levemente em conta inexistente (`timing side-channel` menor em rede local) |
| Reutilizacao de refresh token / sessao comprometida | Rotacao de refresh token de uso unico por `familiaId`, revogacao em cascata em `REUTILIZACAO`, `logout` e `logout-todos` | `CicloTokenApiTest` e `SessaoRefreshTokenTest` | Baixo a medio; `accessToken` JWT permanece valido ate sua expiracao curta (`expiration`) |
| *Password spraying* (muitas contas com poucas tentativas cada) | **Proposta**: rate limit / bloqueio progressivo por `enderecoOrigem` (IP) + atraso/backoff e alerta de pico de `LOGIN_FALHA` em `evento_seguranca` | Cobrivel com teste de carga/integração por IP de origem | Medio se atacante distribuir IPs; exige camada de borda (reverse proxy/WAF) alem do controle por conta |
| Negacao de servico (DoS) por lockout de contas legitimas | **Proposta**: bloqueio apenas **temporario** (`lockDuration` limitado, liberacao automatica ao expirar), sem bloquear permanentemente a conta (`StatusContaAcesso.BLOQUEADA`) por falha de senha, e `permitirTentativa` avaliado antes do `AuthenticationManager` | `ProtecaoAutenticacaoTest.deveAtivarBloqueioExatamenteNaQuintaFalhaELiberarNoFimDoPrazo` | Baixo a medio; um atacante que conheca o `login` pode manter `LOGIN_RECUSADO_BLOQUEIO` enquanto insiste, mitigavel combinando limite por IP/WAF |

## 5. Saida dos Testes Automatizados (`.\mvnw.cmd clean verify`)

Comando executado: `.\mvnw.cmd clean verify` em `C:\Projetos\biblioteca2026`.

```text
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  33.018 s
[INFO] Finished at: 2026-11-26T13:42:19-03:00
[INFO] ------------------------------------------------------------------------
```
