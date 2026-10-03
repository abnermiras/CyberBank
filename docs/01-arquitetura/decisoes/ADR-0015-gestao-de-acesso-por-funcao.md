---
id: 01-arquitetura/decisoes/ADR-0015-gestao-de-acesso-por-funcao
titulo: "ADR-0015: quem tem acesso a um ambiente se lê e se remove por função SECURITY DEFINER"
dono: como o dono le e remove o acesso de outra pessoa sem que a politica de acesso leia a propria tabela
ler-junto: [01-arquitetura/decisoes/ADR-0013-rotina-enxerga-ambientes-por-funcao, 02-dominio/ambiente-financeiro, 03-dados/catalogo-tabelas]
status: ativo
---

# ADR-0015: quem tem acesso a um ambiente se lê e se remove por função `SECURITY DEFINER`

- **Status:** aceita · substitui a política estreita do papel dono do `ADR-0013`
- **Data:** 2026-10-03
- **Afeta:** `03-dados/catalogo-tabelas`, `02-dominio/ambiente-financeiro`, `ADR-0013`

## Contexto

Com o convite, o dono precisa **ver** quem mais tem acesso ao ambiente e **remover** essa
pessoa. As duas coisas leem e apagam linha de `acesso` de **outro** usuário, e o RLS da `V001`
só deixa cada um ver e apagar o próprio acesso. A condição natural — *"sou dono deste
ambiente"* — lê a própria tabela `acesso`, e política que lê a tabela que protege **recursiona**:
o Postgres recusa na reescrita.

Pôr a condição numa função `SECURITY DEFINER` chamada pela política não resolve: a função roda
como o papel dono, que o `FORCE ROW LEVEL SECURITY` sujeita às mesmas políticas — inclusive à
que chamou a função. A recursão sai da reescrita e vai para a execução.

## Decisão

**A leitura e a remoção do acesso alheio são funções, não políticas.** `membros_do_ambiente(id)`
devolve os acessos de um ambiente **só a quem tem acesso a ele**, e `remover_acesso(ambiente,
usuario)` apaga um acesso que **não seja de dono**, e só se quem chama for o próprio usuário
(sair) ou o dono do ambiente. As duas são `SECURITY DEFINER`, criadas pelo papel dono.

Para elas enxergarem e apagarem, o papel dono ganha duas políticas em `acesso`, escritas
**para ele** (`TO`): `SELECT` sem filtro, e `DELETE` de linha que não seja `DONO`. A primeira
**substitui** a `acesso_visivel_para_a_rotina` do `ADR-0013`, que via só as linhas de dono — a
`ambientes_para_rotina()` continua filtrando por `papel = 'DONO'` dentro dela, e devolve o mesmo
que antes.

O papel da aplicação continua sem política nova em `acesso` para ler ou apagar o alheio. O que a
aplicação ganhou foi **endurecer** a de `INSERT`: acesso de editor ou leitor só nasce se existir
convite pendente daquele papel para o e-mail de quem insere; acesso de dono, só no ambiente que
a pessoa acabou de criar e que ainda não tem dono.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Política em `acesso` chamando função `SECURITY DEFINER` | A função roda como dono, o `FORCE` aplica a mesma política a ele, e ela chama a função de novo: recursão em execução |
| Política escrita `TO` o papel da aplicação | O nome do papel vem do ambiente (`docker/postgres-init`), e migration não lê variável de ambiente — a mesma razão do `TO current_user` do `ADR-0013`. Placeholder do Flyway resolveria, mas amarraria o schema à configuração de cada host |
| Coluna `dono_id` em `ambiente` | A política de `acesso` passaria a ler `ambiente`, cuja política lê `acesso`: o mesmo ciclo, um passo mais longo. E duplicaria um fato que `acesso` já guarda |
| Confiar só no domínio | A regra *"só o dono remove"* existe no domínio, mas o `ADR-0002` pede a segunda camada justamente para o dia em que o domínio errar |

## Consequências

- **Ganhamos:** o dono gerencia pessoas sem nenhuma política da aplicação ler a própria tabela,
  e a autoconcessão de acesso — que a `V001` deixava aberta no banco — fecha.
- **Perdemos:** o papel dono passa a ver **todo** `acesso`, não só as linhas de dono. Continua
  valendo que só as funções assumem esse papel, mas cada função `SECURITY DEFINER` nova sobre
  `acesso` precisa trazer o próprio filtro, porque a política não filtra mais por ela.
- **Passa a ser proibido:** função `SECURITY DEFINER` sobre `acesso` sem condição sobre
  `app_usuario_id()`; e, como no `ADR-0013`, função `SECURITY DEFINER` que devolva dado
  financeiro.
- **Revisitar se:** a troca de papel e a transferência de propriedade entrarem — as duas
  escrevem em acesso alheio, e o caminho natural é uma terceira função com a mesma forma.
