/* =========================================================================
   CYBERBANK // a conversa com /api/v1
   Mesma origem: o cookie de sessao (ADR-0009) viaja sozinho, e nao ha header
   de autenticacao nenhum (docs/04-api/convencoes.md).
   ========================================================================= */

/** O cliente le o CODIGO, nunca a mensagem (docs/04-api/erros.md). */
class ErroDaApi extends Error {
  constructor(status, corpo) {
    super((corpo && corpo.codigo) || 'ERRO_INTERNO');
    this.status = status;
    this.codigo = (corpo && corpo.codigo) || 'ERRO_INTERNO';
    this.erros = (corpo && corpo.erros) || [];
  }

  /**
   * A frase e daqui, do cliente — o servidor manda codigo. Erro de campo mostra os campos,
   * porque a validacao devolve TODOS de uma vez e esconder isso desperdica a resposta.
   */
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
  remover: (caminho) => API.requisitar('DELETE', caminho),

  // --- os endpoints, com o nome que o doc deu ---

  cadastrarUsuario: (nome, email, senha) => API.post('/api/v1/usuarios', { nome, email, senha }),
  entrar: (email, senha) => API.post('/api/v1/sessoes', { email, senha }),
  sair: () => API.remover('/api/v1/sessoes/atual'),
  listarAmbientes: () => API.get('/api/v1/ambientes'),

  /**
   * SEMPRE com {@code inativas=true}, e por duas razoes que a tela precisa das duas:
   * a contagem tem que INCLUIR o escondido ("3 de 4" — senao a tela mente), e o
   * interruptor de revelar nao pode custar uma ida ao servidor. Quem esconde e o front.
   *
   * <p>O {@code escolhivel} vem certo assim: o servidor o calcula sobre a arvore INTEIRA
   * antes de filtrar, entao pedir as inativas nao muda o valor de ninguem.
   */
  arvoreDeCategorias: (ambienteId) =>
    API.get(`/api/v1/ambientes/${ambienteId}/categorias?inativas=true`),

  categoriasDeSistema: (ambienteId) =>
    API.get(`/api/v1/ambientes/${ambienteId}/categorias?sistema=true`),

  criarCategoria: (ambienteId, corpo) =>
    API.post(`/api/v1/ambientes/${ambienteId}/categorias`, corpo),

  alterarCategoria: (ambienteId, id, corpo) =>
    API.patch(`/api/v1/ambientes/${ambienteId}/categorias/${id}`, corpo),

  excluirCategoria: (ambienteId, id) =>
    API.remover(`/api/v1/ambientes/${ambienteId}/categorias/${id}`),
};
