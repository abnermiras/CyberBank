---
id: 01-arquitetura/ambientes-de-execucao
titulo: Ambientes de execução
dono: quais ambientes existem, em que maquina cada um roda e o que muda entre eles
ler-junto: [07-operacao/build-e-run, 07-operacao/deploy]
status: ativo
---

# Ambientes de execução

**Dois, e é de propósito.** Um terceiro ambiente de homologação seria uma máquina a mais para
manter, com dado que não é real, para um sistema com um desenvolvedor.

| Ambiente | Onde roda | Para quê |
|---|---|---|
| **Desenvolvimento** | Máquina do Abner (Windows, dev em WSL) | Escrever e rodar. Banco em contêiner, aplicação fora dele |
| **Produção** | Raspberry Pi, em Docker | O sistema de verdade, com o dinheiro de verdade |

A suíte de integração **não é um ambiente**: ela sobe o próprio Postgres descartável a cada
execução (`ADR-0011`).

## O que muda, e o que não

**O que não muda é a lista mais importante**, porque cada item aqui é uma classe inteira de
bug que não pode existir:

- A **versão do Postgres** é a mesma nos dois, e no contêiner de teste.
- O **schema** é o que as migrations dizem, aplicadas na subida, na mesma ordem
  (`docs/03-dados/migrations.md`).
- Os **dois papéis** de banco existem nos dois, e a aplicação nunca é dona das tabelas.
- **HTTPS existe nos dois.** Não há "em desenvolvimento é http" — o cookie de sessão é `Secure`,
  e um desenho que só funciona sem TLS é um desenho não testado
  (`docs/01-arquitetura/seguranca.md`).
- O **dia local é o horário oficial de Brasília** em qualquer host (regra 5 do `CLAUDE.md`). O
  fuso da máquina não participa.

O que muda cabe em três linhas, e todas são **valor de variável**, nunca código ou perfil com
comportamento diferente:

| Muda | Como |
|---|---|
| Endereço e credencial do banco | `DB_URL`, `DB_APP_USER`, `DB_OWNER_USER`… |
| Parâmetros do Argon2id | Calibrados por host: o Pi tem pouca memória, o WSL não |
| `CYBERBANK_URL_BASE` | O que entra no link de recuperação de senha |

**Perfil do Spring não muda comportamento de domínio.** Se uma regra precisa saber em que
ambiente está rodando, ela não é regra de domínio — e é assim que nasce o bug que só aparece em
produção.

## Portas e nomes

Definidos no `compose.yml` de cada lado, e não repetidos aqui: valor que se copia é valor que
diverge (regra 1 do `CONVENTIONS`). O que vale como regra é o que já está em
`docs/01-arquitetura/seguranca.md`: **só a aplicação escuta na rede; o Postgres nunca é
publicado**, nem na rede local.

## Promover uma versão

O caminho está em `docs/07-operacao/deploy.md` (ainda stub). O que já é decisão, e vale desde
já:

- **O push sai da máquina do Abner** — não há credencial de GitHub em mais lugar nenhum.
- **Nada vai para o Pi sem `./mvnw verify` passando**, com a suíte de integração inclusa.
- **Migration é aplicada pela aplicação na subida**, com o papel dono. Não há passo manual em
  produção, porque passo manual é o que se esquece às onze da noite.

## Invariantes

- Existem dois ambientes. Um terceiro exige decisão registrada.
- A versão do Postgres é a mesma em desenvolvimento, produção e teste.
- Os dois papéis de banco existem em todos, e a aplicação nunca é dona das tabelas.
- HTTPS em todos. Não há ambiente sem TLS.
- O que difere entre ambientes é **valor de variável**, nunca código nem perfil com
  comportamento de domínio diferente.
- O dia local é o horário de Brasília em qualquer host.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Comandos, banco local e as variáveis | `07-operacao/build-e-run` |
| Passo a passo do deploy e rollback | `07-operacao/deploy` |
| Por que dois papéis, e por que HTTPS sempre | `03-dados/modelo-de-dados`, `01-arquitetura/seguranca` |
| Backup e restore | `07-operacao/backup-restore` |
