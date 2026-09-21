---
id: 07-operacao/deploy
titulo: Deploy
dono: como uma versao chega em producao, e os parametros do Argon2id calibrados por host
ler-junto: [01-arquitetura/ambientes-de-execucao, 07-operacao/runbook, 01-arquitetura/seguranca]
status: rascunho
---

# Deploy

> **PARCIAL** — só a calibração do Argon2id está escrita. O passo a passo do deploy, o
> rollback e o checklist de promoção continuam sem conteúdo: **pergunte, não deduza**.

## Perguntas que este documento ainda precisa responder

- [ ] Passo a passo do deploy e o script responsavel
- [ ] Como e feito o rollback
- [ ] O que precisa acontecer antes de promover (checklist)
- [ ] Como as migrations sao aplicadas no deploy

## Argon2id: os parâmetros por host

*Por que Argon2id e não bcrypt está em `docs/01-arquitetura/seguranca.md`; os nomes das
variáveis de ambiente, em `docs/07-operacao/build-e-run.md`. Aqui vivem **os números**.*

**O alvo é ~250 ms por hash, e o ajuste é na memória antes das iterações.** 250 ms é o teto
do que não se percebe num login e o piso do que dói em quem tenta força bruta. Subir memória
é o que encarece GPU; subir iteração encarece o servidor na mesma proporção que o atacante.
Por isso a ordem: escolha a maior memória que o host aguenta, e só então ajuste a iteração
até bater o alvo.

| Host | `ARGON2_MEMORIA` (KB) | `ARGON2_ITERACOES` | `ARGON2_PARALELISMO` | Medido |
|---|---|---|---|---|
| Desenvolvimento (4 núcleos, 7,4 GB) | 65536 | 3 | 1 | 154 ms · 20/09/2026 |
| Produção (Raspberry Pi) | — | — | — | **não medido** |

**O número novo alcança quem já tem conta pelo login, um a um.** O hash guarda os próprios
`m`, `t` e `p`; quem entra com hash abaixo do parâmetro atual tem a senha regravada ali mesmo
(`docs/01-arquitetura/seguranca.md`). Recalibrar, portanto, não é só editar a variável: a base
converge no ritmo em que as pessoas entram, e quem não voltar fica no número antigo.

A linha de desenvolvimento é `t=3` porque é o que os hashes existentes já traziam quando a
regravação passou a existir. `t=4` mediu 226 ms e é uma troca defensável — só não é de graça,
e a tabela é o lugar onde ela se registra.

**A linha do Pi está vazia porque ninguém mediu ainda, e número copiado da linha de cima é
exatamente o erro que esta tabela existe para impedir.** Medir é passo de instalação, antes
do primeiro usuário: o hash dos que já existem carrega os parâmetros antigos e só é
reescrito no login seguinte.

**`ARGON2_PARALELISMO` é 1, e não o número de núcleos.** A paralelização é por hash: com `p`
alto, um login sozinho ocupa `p` núcleos e o host atende menos logins simultâneos pelo mesmo
custo de resistência. O paralelismo útil aqui vem de atender vários logins ao mesmo tempo,
não de acelerar um.

**A memória é por hash em voo, não por processo.** `ARGON2_MEMORIA` × logins simultâneos é
o que o host precisa ter sobrando — e o login que falha custa igual ao que acerta, porque o
sistema confere contra um hash de mentira quando o usuário não existe
(`docs/01-arquitetura/seguranca.md`). No Pi é esse produto que limita a memória, não o que a
máquina tem livre em repouso.

### Como medir

Contra o `Argon2PasswordEncoder` com os mesmos tamanhos de sal e hash do
`SenhasArgon2` — parâmetro medido com outro encoder não vale:

```java
var e = new Argon2PasswordEncoder(16, 32, paralelismo, memoria, iteracoes);
e.encode("aquecimento");
long ini = System.nanoTime();
e.encode("senha-de-calibracao-123");
long ms = (System.nanoTime() - ini) / 1_000_000;
```

Descarte a primeira execução (JIT), tome a **mediana de cinco**, e meça no host de destino
com a aplicação parada. O classpath sai de `./mvnw dependency:build-classpath`.

**Recalibre quando o host mudar** — troca de Pi, mudança de memória, atualização de JDK. O
parâmetro que protegia num host é decoração em outro.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Por que Argon2id, e o que mais protege a senha | `01-arquitetura/seguranca` |
| Os nomes das variáveis e como carregá-las | `07-operacao/build-e-run` |
| Quais ambientes existem e o que muda entre eles | `01-arquitetura/ambientes-de-execucao` |
| O que fazer quando quebra em produção | `07-operacao/runbook` |
