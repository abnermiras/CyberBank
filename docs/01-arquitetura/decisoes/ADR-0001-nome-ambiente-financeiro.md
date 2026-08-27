---
id: 01-arquitetura/decisoes/ADR-0001-nome-ambiente-financeiro
titulo: "ADR-0001: manter o nome ambiente financeiro"
dono: a decisão sobre a colisão do termo ambiente
ler-junto: [00-produto/glossario, 02-dominio/ambiente-financeiro]
status: ativo
---

# ADR-0001: manter o nome "ambiente financeiro"

- **Status:** aceita
- **Data:** 2026-08-27
- **Afeta:** todo o domínio, o schema, a API e as mensagens ao usuário

## Contexto

"Ambiente" ficou com dois sentidos no projeto: o **ambiente financeiro** (o espaço de
dados que é dono de todo dado do usuário) e o **ambiente de execução** (dev, homologação,
produção). O termo do domínio aparece em cada classe, cada tabela e cada endpoint, então
a escolha precisava sair antes da primeira linha de código.

## Decisão

Mantemos **"ambiente financeiro"** para o conceito de domínio. O outro sentido é sempre
escrito por extenso como **"ambiente de execução"**, nunca abreviado para "ambiente".

No código e no schema, o termo do domínio é o não-marcado: `Ambiente`, `ambiente_id`,
`AmbienteRepository`. Configuração de execução usa o vocabulário do Spring (`profile`,
`environment`) e não disputa o nome.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Renomear para "Espaço" | Resolve a colisão e é neutro, mas "espaço financeiro" não é como ninguém fala do próprio dinheiro — o ganho é do compilador, o custo é do usuário |
| Renomear para "Carteira" | Colide de novo: o glossário já usa "carteira" como tipo de **conta**. Trocar uma colisão por outra, dessa vez dentro do próprio domínio |
| Renomear para "Cofre" | Sem colisão nenhuma, mas sugere guardar dinheiro; o conceito é organizar dado, e ele contém dívida e fatura também |

## Consequências

- **Ganhamos:** o termo que o usuário entende sem tradução, e zero renomeação agora.
- **Perdemos:** a ambiguidade não some — ela vira disciplina de escrita, e disciplina
  falha. Conversa e busca no repositório vão exigir o qualificador.
- **Passa a ser proibido:** usar "ambiente" sozinho para dev/homologação/produção, em
  doc, commit, log ou nome de variável. Nesse sentido, sempre "ambiente de execução".
- **Revisitar se:** o projeto ganhar um segundo desenvolvedor e a confusão aparecer em
  code review, ou se um bug real for causado pela ambiguidade. Aí o custo da renomeação
  fica menor que o da disciplina.
