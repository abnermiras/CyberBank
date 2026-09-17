const Diario = {
  FUSO: 'America/Sao_Paulo',

  dia: null,
  itens: [],
  autores: {},
  ligado: false,

  TELA_DO_ALVO: {
    LANCAMENTO: (id) => `#/extrato/${id}`,
    CONTA: '#/cadastro',
    MEIO: '#/cadastro',
    CATEGORIA: '#/cadastro',
  },

  FRASES: {
    LANCAMENTO_REALIZADO: (d) =>
      `<b>${Diario.desc(d)}</b> venceu: o previsto virou realizado`,
    LANCAMENTO_CRIADO: (d) => (d.transferenciaId
      ? `transferiu <b>${Diario.desc(d)}</b>`
      : `lançou <b>${Diario.desc(d)}</b>${d.situacao === 'PREVISTO' ? ', como previsto' : ''}`),
    LANCAMENTO_EDITADO: (d) => `corrigiu <b>${Diario.desc(d)}</b>${Diario.deParas(d)}`,
    LANCAMENTO_ESTORNADO: (d) => `estornou <b>${Diario.desc(d)}</b>`,
    LANCAMENTO_EXCLUIDO: (d) => `excluiu <b>${Diario.desc(d)}</b>`,

    CONTA_CRIADA: (d) => `abriu a conta <b>${Formato.texto(d.nome)}</b>`,
    CONTA_RENOMEADA: (d) => `renomeou a conta${Diario.deParas(d)}`,
    CONTA_INATIVADA: (d) => `inativou a conta <b>${Formato.texto(d.nome)}</b>`,
    CONTA_REATIVADA: (d) => `reativou a conta <b>${Formato.texto(d.nome)}</b>`,
    CONTA_EXCLUIDA: (d) => `excluiu a conta <b>${Formato.texto(d.nome)}</b>`,

    MEIO_CRIADO: (d) =>
      `cadastrou <b>${Formato.texto(d.nome || d.tipo)}</b> em ${Formato.texto(d.conta)}`,
    MEIO_RENOMEADO: (d) => `renomeou o meio${Diario.deParas(d)}`,
    MEIO_INATIVADO: (d) => `inativou o meio <b>${Formato.texto(d.nome || d.tipo)}</b>`,
    MEIO_REATIVADO: (d) => `reativou o meio <b>${Formato.texto(d.nome || d.tipo)}</b>`,
    MEIO_EXCLUIDO: (d) => `excluiu o meio <b>${Formato.texto(d.nome || d.tipo)}</b>`,

    CATEGORIA_CRIADA: (d) => `criou a categoria <b>${Formato.texto(d.nome)}</b>`,
    CATEGORIA_RENOMEADA: (d) => `renomeou a categoria${Diario.deParas(d)}`,
    CATEGORIA_RECOLORIDA: (d) => `trocou a cor de <b>${Formato.texto(d.nome)}</b>${Diario.deParas(d)}`,
    CATEGORIA_INATIVADA: (d) => `inativou a categoria <b>${Formato.texto(d.nome)}</b>`,
    CATEGORIA_REATIVADA: (d) => `reativou a categoria <b>${Formato.texto(d.nome)}</b>`,
    CATEGORIA_EXCLUIDA: (d) => `excluiu a categoria <b>${Formato.texto(d.nome)}</b>`,
  },

  ROTULO_DO_CAMPO: {
    nome: 'nome',
    valor: 'valor',
    descricao: 'descrição',
    sentido: 'sentido',
    situacao: 'situação',
    contaId: 'conta',
    categoriaId: 'categoria',
    dataEvento: 'data do evento',
    dataEfeito: 'data de efeito',
    cor: 'cor',
  },

  async montar() {
    if (!Diario.ligado) {
      Diario.ligarOuvintes();
      Diario.ligado = true;
    }
    if (!Diario.dia) Diario.dia = Formato.hoje();
    await Diario.recarregar();
  },

  ligarOuvintes() {
    CampoDeData.ligar('diarioData');

    document.getElementById('diarioData').addEventListener('change', () => {
      const escolhido = CampoDeData.valor('diarioData');
      if (escolhido) Diario.irPara(escolhido);
    });

    document.getElementById('diaAnterior').addEventListener('click',
      () => Diario.irPara(CampoDeData.somarDias(Diario.dia, -1)));
    document.getElementById('diaSeguinte').addEventListener('click',
      () => Diario.irPara(CampoDeData.somarDias(Diario.dia, 1)));
    document.getElementById('diaHoje').addEventListener('click',
      () => Diario.irPara(Formato.hoje()));
  },

  async irPara(dia) {
    if (dia > Formato.hoje()) return;
    Diario.dia = dia;
    await Diario.recarregar();
  },

  async recarregar() {
    CampoDeData.definir('diarioData', Diario.dia);
    document.getElementById('diaSeguinte').disabled = Diario.dia >= Formato.hoje();

    try {
      const diario = await API.diario(Contexto.ambiente.id, Diario.dia);
      Diario.dia = diario.dia;
      Diario.itens = diario.itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Diario.avisar(erro.paraGente());
      return;
    }
    Diario.avisar('');
    Diario.desenhar();
  },

  desenhar() {
    const doSistema = Diario.itens.filter((e) => e.origem === 'SISTEMA');
    const doUsuario = Diario.itens.filter((e) => e.origem === 'USUARIO');

    document.getElementById('contagemDiario').textContent = Diario.itens.length
      ? `${Diario.itens.length} EVENTO${Diario.itens.length > 1 ? 'S' : ''}`
      : 'DIA SEM MOVIMENTO';

    document.getElementById('diarioSistema').innerHTML = Diario.secao(
      'O QUE O SISTEMA FEZ', doSistema,
      'O sistema não mexeu em nada neste dia.');

    document.getElementById('diarioUsuario').innerHTML = Diario.secao(
      'O QUE VOCÊ FEZ', doUsuario,
      'Ninguém mexeu em nada neste dia.');
  },

  secao(titulo, eventos, vazio) {
    return `
      <div class="diario-secao">
        <h4>${titulo}</h4>
        ${eventos.length
          ? eventos.map(Diario.linha).join('')
          : `<div class="vazio">${vazio}</div>`}
      </div>`;
  },

  linha(e) {
    const dados = e.dados || {};
    const frase = Diario.FRASES[e.tipo];
    const destino = Diario.destino(e);

    return `
      <article class="evento${e.origem === 'SISTEMA' ? ' do-sistema' : ''}">
        <div class="evento-hora">${Diario.hora(e.instante)}</div>
        <div class="evento-corpo">
          <div class="evento-frase">${frase ? frase(dados) : Formato.texto(e.tipo)}</div>
          <div class="hstack gap6 wrap">
            ${e.origem === 'SISTEMA'
              ? '<span class="tag">SISTEMA</span>'
              : `<span class="tele">${Formato.texto(e.autor || '—')}</span>`}
            ${destino
              ? `<a class="chip-mais" href="${destino}">abrir</a>`
              : '<span class="tele">SEM DESTINO</span>'}
          </div>
        </div>
        <div class="evento-valor ${Diario.corDoValor(dados)}">
          ${Diario.valorDaLinha(dados)}
        </div>
      </article>`;
  },

  valorDaLinha(dados) {
    if (dados.valor == null) return '';
    const cru = Formato.dinheiro(dados.valor).replace('−', '');
    if (dados.transferenciaId) return cru;
    return `${dados.sentido === 'SAIDA' ? '−' : '+'}${cru}`;
  },

  corDoValor(dados) {
    if (dados.transferenciaId) return '';
    return dados.sentido === 'SAIDA' ? 'neg' : 'pos';
  },

  destino(e) {
    if (!e.alvo || e.tipo.endsWith('_EXCLUIDO') || e.tipo.endsWith('_EXCLUIDA')) return null;
    const destino = Diario.TELA_DO_ALVO[e.alvo.tipo];
    if (!destino) return null;
    return typeof destino === 'function' ? destino(e.alvo.id) : destino;
  },

  deParas(dados) {
    const campos = Object.keys(dados)
      .filter((chave) => chave.endsWith('Para'))
      .map((chave) => chave.slice(0, -4));

    if (!campos.length) return '';

    return `: ${campos.map((campo) => `${Diario.ROTULO_DO_CAMPO[campo] || campo} `
      + `<s>${Diario.valor(campo, dados[`${campo}De`])}</s> → `
      + `<b>${Diario.valor(campo, dados[`${campo}Para`])}</b>`).join(' · ')}`;
  },

  valor(campo, bruto) {
    if (bruto == null) return '—';
    if (campo === 'valor') return Formato.dinheiro(bruto);
    if (campo === 'dataEvento' || campo === 'dataEfeito') return Formato.dia(bruto);
    if (campo === 'categoriaId') return Formato.texto(Diario.nomeDaCategoria(bruto));
    if (campo === 'contaId') return Formato.texto(Diario.nomeDaConta(bruto));
    return Formato.texto(bruto);
  },

  nomeDaCategoria(id) {
    for (const raiz of Extrato.arvore || []) {
      if (raiz.id === id) return raiz.nome;
      const filha = raiz.filhas.find((f) => f.id === id);
      if (filha) return `${raiz.nome} › ${filha.nome}`;
    }
    return `#${id}`;
  },

  nomeDaConta(id) {
    const conta = (Extrato.contas || []).find((c) => c.id === id);
    return conta ? conta.nome : `#${id}`;
  },

  desc(dados) {
    return Formato.texto(dados.descricao || 'lançamento');
  },

  hora(instante) {
    return new Date(instante).toLocaleTimeString('pt-BR', {
      timeZone: Diario.FUSO, hour: '2-digit', minute: '2-digit',
    });
  },

  avisar(mensagem) {
    document.getElementById('avisoDiario').innerHTML = mensagem
      ? `<div class="aviso err">${Formato.texto(mensagem)}</div>`
      : '';
  },
};
