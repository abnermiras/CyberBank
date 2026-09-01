---
id: 01-arquitetura/decisoes/ADR-0007-email-so-para-recuperar-senha
titulo: "ADR-0007: o sistema envia e-mail, e só para recuperar senha"
dono: a dependencia de SMTP, e o escopo fechado de para que ela serve
ler-junto: [02-dominio/ambiente-financeiro, 01-arquitetura/seguranca]
status: ativo
---

# ADR-0007: o sistema envia e-mail, e só para recuperar senha

- **Status:** aceita
- **Data:** 2026-09-01
- **Afeta:** autenticação, `02-dominio/ambiente-financeiro`, `05-integracoes/vault-segredos`

## Contexto

A autenticação é por e-mail e senha, e o e-mail é o identificador de login. Faltava a
resposta de uma pergunta que nenhum doc tinha: **como alguém recupera a senha esquecida.**

Ela não tem contorno dentro do sistema, e é o que a separa de todas as outras. O **convite**
resolveu a ausência de e-mail vivendo no app: o convidado entra, vê o convite no perfil,
aceita ou recusa. Quem esqueceu a senha **não consegue entrar** — não há tela onde pôr o
aviso. Sem canal externo, a pessoa fica de fora e só um acesso ao banco a traz de volta.

## Decisão

O Cyberbank envia e-mail por SMTP do Gmail, com **senha de app** de uma conta Google do dono
da instância. Custo zero, sem serviço pago, roda no Pi — as restrições da `visao.md` continuam
de pé.

**E o escopo é fechado em um uso:**

> **O e-mail só é usado quando a pessoa não consegue entrar no sistema.** Hoje isso é
> exatamente um caso: **recuperar senha.**

Tudo o mais chega **pelo sistema**, sem exceção e para sempre: convite para um ambiente,
compartilhamento de conta ou cartão, vínculo, e qualquer aviso a quem já está dentro.

**Isso não é consequência de não haver e-mail.** É a decisão, e a razão dela é de produto:
**informação que é do sistema chega pelo sistema.** O convite vive no app porque é lá que ele
é aceito ou recusado, porque convite pendente não é acesso, e porque ele continua funcionando
se o SMTP cair. Nada disso muda com o e-mail existindo — e nada disso mudaria se o Cyberbank
fosse para a nuvem, para outro provedor ou para lugar nenhum.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Sem e-mail nenhum: o dono reseta a senha no banco | É o que existia por omissão. Transforma esquecer a senha em chamado para o administrador, e num sistema multiusuário isso acontece |
| Serviço de e-mail transacional (SES, SendGrid) | Cadastro, custo e uma dependência externa a mais para uma mensagem por semestre |
| O e-mail passa a levar convite também, já que agora existe | Trocaria um canal que funciona por um que depende do SMTP estar de pé, e espalharia o e-mail por todo aviso do sistema. O convite não vivia no app por falta de e-mail |

## Consequências

- **Ganhamos:** a única porta que não tinha saída passa a ter uma, e ela é a mais barata.
- **Perdemos:** uma dependência externa e um segredo a guardar — a senha de app envia e-mail
  em nome do dono da instância. Onde ela mora é `docs/05-integracoes/vault-segredos.md`.
- **Passa a ser proibido:** usar e-mail para qualquer coisa que a pessoa possa ver estando
  dentro do sistema. Uso novo de e-mail exige mover esta linha, não abrir uma exceção.
- **O SMTP nunca é caminho crítico:** login, lançamento e rotina funcionam com ele fora do ar.
  Só a recuperação de senha para.
- **Revisitar se:** aparecer um segundo caso em que a pessoa precisa ser avisada **sem
  conseguir entrar**. Aviso a quem consegue entrar nunca é esse caso.
