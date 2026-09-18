class ErroDaApi extends Error {
  constructor(status, corpo) {
    super((corpo && corpo.codigo) || 'ERRO_INTERNO');
    this.status = status;
    this.codigo = (corpo && corpo.codigo) || 'ERRO_INTERNO';
    this.erros = (corpo && corpo.erros) || [];
  }

  paraGente() {
    if (this.codigo === 'VALIDACAO' && this.erros.length) {
      return this.erros.map((e) => e.mensagem).join(' ');
    }
    return API.MENSAGENS[this.codigo] || 'Falha inesperada. Tente de novo.';
  }
}

const API = {
  MENSAGENS: {
    NAO_AUTENTICADO: 'Sua sessão expirou. Entre de novo.',
    CREDENCIAIS_INVALIDAS: 'E-mail ou senha inválidos.',
    MUITAS_TENTATIVAS: 'Tentativas demais. Espere um pouco.',
    SEM_PERMISSAO: 'Seu papel neste ambiente não permite isso.',
    NAO_ENCONTRADO: 'Não encontrado.',
    EMAIL_JA_CADASTRADO: 'Este e-mail já está cadastrado.',
    CATEGORIA_DE_SISTEMA_PROTEGIDA:
      'Categoria de sistema não se renomeia, não se inativa e não se exclui.',
    CATEGORIA_PAI_INVALIDO: 'A árvore tem só dois níveis: subcategoria não tem filhas.',
    CATEGORIA_COM_SUBCATEGORIA:
      'Esta raiz ainda tem subcategorias. Esvazie a árvore antes, ou inative a raiz.',
    CATEGORIA_COM_LANCAMENTO: 'Esta categoria já tem lançamento. O caminho é inativar.',
    CATEGORIA_SEM_COR_PROPRIA: 'Subcategoria herda a cor da raiz e não tem cor própria.',
    CATEGORIA_NAO_ESCOLHIVEL:
      'Esta categoria não é destino de lançamento: ou está inativa, ou tem subcategoria ativa.',
    CONTA_INATIVA: 'Esta conta está inativa e não recebe lançamento novo.',
    CONTA_NAO_E_APLICACAO:
      'Só uma aplicação tem valor informado: o saldo das outras é a soma do que se movimentou.',
    CONTA_COM_LANCAMENTO: 'Esta conta já tem lançamento. O caminho é inativar.',
    CARTAO_SEM_SALDO_INICIAL:
      'Cartão de crédito não tem saldo de abertura: a dívida é o conjunto das faturas.',
    MEIO_INCOMPATIVEL_COM_CONTA: 'Este meio de pagamento não serve para esta conta.',
    MEIO_COM_LANCAMENTO: 'Este meio já tem lançamento. O caminho é inativar.',
    MEIO_INATIVO: 'Este meio está inativo e não recebe lançamento novo.',
    LANCAMENTO_DO_CICLO:
      'Este lançamento foi criado pelo sistema. O saldo de abertura se corrige editando o valor.',
    LANCAMENTO_COM_ESTORNO: 'Exclua primeiro o estorno, senão ele fica órfão.',
    TRANSFERENCIA_MESMA_CONTA: 'Origem e destino são a mesma conta.',
    BENEFICIO_NAO_TRANSFERE:
      'O saldo de um benefício não é fungível: entra por receita e sai por gasto no meio dele.',
    SENHA_ATUAL_INVALIDA: 'A senha atual não confere.',
    TELEGRAM_JA_VINCULADO: 'Este chat do Telegram já está vinculado a outro usuário.',
    CORPO_INVALIDO: 'Requisição malformada.',
    ERRO_INTERNO: 'Falha nossa. Tente de novo.',
  },

  async requisitar(metodo, caminho, corpo) {
    const resposta = await fetch(caminho, {
      method: metodo,
      credentials: 'same-origin',
      headers: corpo === undefined ? {} : { 'Content-Type': 'application/json' },
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });

    if (resposta.status === 204) return null;

    const texto = await resposta.text();
    const dados = texto ? JSON.parse(texto) : null;
    if (!resposta.ok) throw new ErroDaApi(resposta.status, dados);
    return dados;
  },

  get: (caminho) => API.requisitar('GET', caminho),
  post: (caminho, corpo) => API.requisitar('POST', caminho, corpo),
  patch: (caminho, corpo) => API.requisitar('PATCH', caminho, corpo),
  put: (caminho, corpo) => API.requisitar('PUT', caminho, corpo),
  remover: (caminho) => API.requisitar('DELETE', caminho),

  doAmbiente: (ambienteId, sufixo) => `/api/v1/ambientes/${ambienteId}${sufixo}`,

  cadastrarUsuario: (nome, email, senha) => API.post('/api/v1/usuarios', { nome, email, senha }),
  entrar: (email, senha) => API.post('/api/v1/sessoes', { email, senha }),
  sair: () => API.remover('/api/v1/sessoes/atual'),
  listarAmbientes: () => API.get('/api/v1/ambientes'),

  verPerfil: () => API.get('/api/v1/usuarios/atual'),
  alterarPerfil: (corpo) => API.patch('/api/v1/usuarios/atual', corpo),
  vincularTelegram: (chatId) => API.put('/api/v1/usuarios/atual/telegram', { chatId }),
  desvincularTelegram: () => API.remover('/api/v1/usuarios/atual/telegram'),
  trocarSenha: (senhaAtual, novaSenha) =>
    API.put('/api/v1/usuarios/atual/senha', { senhaAtual, novaSenha }),

  arvoreDeCategorias: (ambienteId) =>
    API.get(API.doAmbiente(ambienteId, '/categorias?inativas=true')),

  categoriasDeSistema: (ambienteId) =>
    API.get(API.doAmbiente(ambienteId, '/categorias?sistema=true')),

  criarCategoria: (ambienteId, corpo) =>
    API.post(API.doAmbiente(ambienteId, '/categorias'), corpo),

  alterarCategoria: (ambienteId, id, corpo) =>
    API.patch(API.doAmbiente(ambienteId, `/categorias/${id}`), corpo),

  excluirCategoria: (ambienteId, id) =>
    API.remover(API.doAmbiente(ambienteId, `/categorias/${id}`)),

  listarContas: (ambienteId) => API.get(API.doAmbiente(ambienteId, '/contas?inativas=true')),

  abrirConta: (ambienteId, corpo) => API.post(API.doAmbiente(ambienteId, '/contas'), corpo),

  alterarConta: (ambienteId, id, corpo) =>
    API.patch(API.doAmbiente(ambienteId, `/contas/${id}`), corpo),

  excluirConta: (ambienteId, id) => API.remover(API.doAmbiente(ambienteId, `/contas/${id}`)),

  informarLimite: (ambienteId, contaId, limite) =>
    API.put(API.doAmbiente(ambienteId, `/contas/${contaId}/limite`), { limite }),

  parcelar: (ambienteId, corpo) =>
    API.post(API.doAmbiente(ambienteId, '/parcelamentos'), corpo),

  excluirParcelamento: (ambienteId, id) =>
    API.remover(API.doAmbiente(ambienteId, `/parcelamentos/${id}`)),

  listarFaturas: (ambienteId, contaId) =>
    API.get(API.doAmbiente(ambienteId, `/faturas?contaId=${contaId}`)),

  lancamentosDaFatura: (ambienteId, faturaId) =>
    API.get(API.doAmbiente(ambienteId, `/faturas/${faturaId}/lancamentos`)),

  pagarFatura: (ambienteId, faturaId, corpo) =>
    API.post(API.doAmbiente(ambienteId, `/faturas/${faturaId}/pagamentos`), corpo),

  fecharFatura: (ambienteId, faturaId) =>
    API.post(API.doAmbiente(ambienteId, `/faturas/${faturaId}/fechamento`), {}),

  abrirFatura: (ambienteId, faturaId) =>
    API.post(API.doAmbiente(ambienteId, `/faturas/${faturaId}/abertura`), {}),

  listarMeios: (ambienteId) =>
    API.get(API.doAmbiente(ambienteId, '/meios-de-pagamento?inativos=true')),

  cadastrarMeio: (ambienteId, corpo) =>
    API.post(API.doAmbiente(ambienteId, '/meios-de-pagamento'), corpo),

  alterarMeio: (ambienteId, id, corpo) =>
    API.patch(API.doAmbiente(ambienteId, `/meios-de-pagamento/${id}`), corpo),

  excluirMeio: (ambienteId, id) =>
    API.remover(API.doAmbiente(ambienteId, `/meios-de-pagamento/${id}`)),

  reserva: (ambienteId) => API.get(API.doAmbiente(ambienteId, '/contas/reserva')),

  informarValorDaAplicacao: (ambienteId, contaId, corpo) =>
    API.put(API.doAmbiente(ambienteId, `/contas/${contaId}/valor-atual`), corpo),

  resumoDoMes: (ambienteId, mes) =>
    API.get(API.doAmbiente(ambienteId, '/relatorios/resumo' + (mes ? `?mes=${mes}` : ''))),

  extrato: (ambienteId, parametros) =>
    API.get(API.doAmbiente(ambienteId, `/lancamentos?${new URLSearchParams(parametros)}`)),

  lancar: (ambienteId, corpo) => API.post(API.doAmbiente(ambienteId, '/lancamentos'), corpo),

  transferir: (ambienteId, corpo) =>
    API.post(API.doAmbiente(ambienteId, '/lancamentos/transferencias'), corpo),

  editarLancamento: (ambienteId, id, corpo) =>
    API.patch(API.doAmbiente(ambienteId, `/lancamentos/${id}`), corpo),

  verLancamento: (ambienteId, id) =>
    API.get(API.doAmbiente(ambienteId, `/lancamentos/${id}`)),

  excluirLancamento: (ambienteId, id) =>
    API.remover(API.doAmbiente(ambienteId, `/lancamentos/${id}`)),

  estornarLancamento: (ambienteId, id, corpo) =>
    API.post(API.doAmbiente(ambienteId, `/lancamentos/${id}/estorno`), corpo),

  diario: (ambienteId, dia) => API.get(API.doAmbiente(ambienteId, `/eventos?dia=${dia}`)),

  historicoDoAlvo: (ambienteId, tipo, alvoId) =>
    API.get(API.doAmbiente(ambienteId, `/eventos?alvo=${tipo}&alvoId=${alvoId}`)),
};
