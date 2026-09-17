const Detalhe = {
  SITUACAO: { PREVISTO: 'prev', PROVISIONADO: 'prov', REALIZADO: 'real' },

  id: null,
  dados: null,
  historico: [],
  confirmando: false,
  ligado: false,

  aberto() {
    return !document.getElementById('modalDetalhe').hidden;
  },

  async abrir(id) {
    Detalhe.ligar();
    Detalhe.id = id;
    Detalhe.confirmando = false;
    document.getElementById('modalDetalhe').hidden = false;
    document.getElementById('detalheCorpo').innerHTML =
      '<span class="tele">CARREGANDO…</span>';

    let historico;
    try {
      [Detalhe.dados, historico] = await Promise.all([
        API.verLancamento(Contexto.ambiente.id, id),
        API.historicoDoAlvo(Contexto.ambiente.id, 'LANCAMENTO', id),
      ]);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Detalhe.dados = null;
      document.getElementById('detalheCorpo').innerHTML = `
        <div class="vazio">${Formato.texto(erro.paraGente())}<br>
          <b>Este lançamento não está mais aqui.</b></div>`;
      return;
    }

    Detalhe.historico = historico.itens;
    Detalhe.pintar();
  },

  esconder() {
    document.getElementById('modalDetalhe').hidden = true;
    Detalhe.id = null;
    Detalhe.dados = null;
    Detalhe.historico = [];
  },

  fechar() {
    Detalhe.esconder();
    if ((window.location.hash || '').startsWith('#/extrato/')) {
      window.location.hash = '#/extrato';
    }
  },

  ligar() {
    if (Detalhe.ligado) return;
    Detalhe.ligado = true;

    document.getElementById('btnFecharDetalhe')
      .addEventListener('click', Detalhe.fechar);

    document.getElementById('modalDetalhe').addEventListener('click', (evento) => {
      if (evento.target.id === 'modalDetalhe') Detalhe.fechar();
    });

    document.addEventListener('keydown', (evento) => {
      if (evento.key === 'Escape' && Detalhe.aberto()) Detalhe.fechar();
    });

    document.getElementById('detalheCorpo').addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-detalhe]');
      if (!botao) return;
      const acoes = {
        confirmar: () => { Detalhe.confirmando = true; Detalhe.pintar(); },
        cancelar: () => { Detalhe.confirmando = false; Detalhe.pintar(); },
        excluir: Detalhe.excluir,
        estornar: Detalhe.estornar,
        ir: () => Detalhe.abrir(Number(botao.dataset.id)),
      };
      await acoes[botao.dataset.detalhe]();
    });

    document.getElementById('detalheCorpo').addEventListener('change', async (evento) => {
      const seletor = evento.target.closest('[data-categoria-de]');
      if (!seletor || !seletor.value) return;
      seletor.disabled = true;
      await Detalhe.categorizar(Number(seletor.value));
    });
  },

  pintar() {
    const d = Detalhe.dados;
    const saida = d.sentido === 'SAIDA';

    document.getElementById('detalheTitulo').textContent = `Lançamento #${d.id}`;
    document.getElementById('detalheCorpo').innerHTML = `
      <div id="avisoDetalhe"></div>

      <div class="det-topo">
        <div>
          <div class="det-desc">${Formato.texto(d.descricao)}</div>
          <div class="hstack gap6 wrap mt10">
            <span class="tag ${Detalhe.SITUACAO[d.situacao]}">${d.situacao}</span>
            ${d.transferencia ? '<span class="tag transf">TRANSFERÊNCIA</span>' : ''}
            ${d.estornoDeId ? '<span class="tag transf">ESTORNO</span>' : ''}
            ${d.estornadoPorId ? '<span class="tag inativa">ESTORNADO</span>' : ''}
            ${d.doCiclo ? '<span class="tag">DO SISTEMA</span>' : ''}
          </div>
        </div>
        <div class="det-valor ${saida ? 'neg' : 'pos'}">
          ${saida ? '−' : '+'}${Formato.dinheiro(d.valor).replace('−', '')}
        </div>
      </div>

      ${Detalhe.bloco('O dinheiro', Detalhe.dinheiro())}
      ${Detalhe.bloco('As duas datas', Detalhe.datas())}
      ${Detalhe.bloco('Classificação', Detalhe.classificacao())}
      ${Detalhe.bloco('Quem e quando', Detalhe.quemEQuando())}
      ${Detalhe.ligacoes()}
      ${Detalhe.serieEFatura()}
      ${Detalhe.oQueJaAconteceu()}
      ${Detalhe.acoes()}`;
  },

  bloco(titulo, linhas) {
    return `<section class="det-bloco"><h4>${titulo}</h4><dl>${linhas}</dl></section>`;
  },

  par(rotulo, valor) {
    return `<dt>${rotulo}</dt><dd>${valor}</dd>`;
  },

  dinheiro() {
    const d = Detalhe.dados;
    const meio = Detalhe.rotuloDoMeio();
    return Detalhe.par('Conta', d.conta
        ? `${Formato.texto(d.conta.nome)} <i class="det-fraco">${d.conta.tipo}</i>`
        : '<i class="det-fraco">de outro ambiente</i>')
      + Detalhe.par('Meio', meio
        ? Formato.texto(meio)
        : '<i class="det-fraco">nenhum — ninguém pagou nada aqui</i>')
      + Detalhe.par('Sentido', d.sentido === 'SAIDA' ? 'SAÍDA' : 'ENTRADA')
      + Detalhe.par('Valor', Formato.dinheiro(d.valor).replace('−', ''));
  },

  rotuloDoMeio() {
    const d = Detalhe.dados;
    if (!d.meio) return null;
    if (d.meio.nome) return `${d.conta ? d.conta.nome : '—'} · ${d.meio.nome}`;
    const tipo = Contas.ROTULO_DE_MEIO[d.meio.tipo] || d.meio.tipo;
    return `${d.conta ? d.conta.nome : '—'} · ${tipo}`;
  },

  datas() {
    const d = Detalhe.dados;
    const iguais = d.dataEvento === d.dataEfeito;
    return Detalhe.par('Aconteceu em', Formato.dia(d.dataEvento))
      + Detalhe.par('Mexe no saldo em', Formato.dia(d.dataEfeito))
      + (iguais ? '' : `<dt></dt><dd class="det-explica">As duas datas são diferentes porque o
          meio separa o dia da compra do dia do pagamento. É por
          <b>${Formato.dia(d.dataEfeito)}</b> que o saldo se calcula; por
          <b>${Formato.dia(d.dataEvento)}</b> é que você procura.</dd>`);
  },

  classificacao() {
    const d = Detalhe.dados;
    if (!d.categoria) {
      return Detalhe.par('Categoria', `
        <div class="det-pendente">
          <span class="tag pend">SEM CATEGORIA</span>
          <select class="filtro" data-categoria-de="${d.id}">
            ${Detalhe.opcoes(d.sentido)}
          </select>
        </div>`);
    }
    const raiz = d.categoria.raiz;
    const caminho = raiz.id === d.categoria.id
      ? Formato.texto(raiz.nome)
      : `${Formato.texto(raiz.nome)} › ${Formato.texto(d.categoria.nome)}`;

    return Detalhe.par('Categoria',
      `<span class="det-cat" style="--k:${Formato.tom(raiz.cor)}">${caminho}</span>`);
  },

  opcoes(sentido) {
    const raizes = (Extrato.arvore || [])
      .filter((raiz) => !raiz.sistema && raiz.sentido === sentido && !raiz.inativa);

    if (!raizes.length) return '<option value="">— nenhuma categoria deste sentido —</option>';

    return '<option value="">— escolher —</option>'
      + raizes.filter((raiz) => raiz.escolhivel)
        .map((raiz) => `<option value="${raiz.id}">${Formato.texto(raiz.nome)}</option>`).join('')
      + raizes.filter((raiz) => !raiz.escolhivel).map((raiz) => {
        const filhas = raiz.filhas.filter((filha) => filha.escolhivel);
        if (!filhas.length) return '';
        return `<optgroup label="${Formato.texto(raiz.nome)}">`
          + filhas.map((filha) =>
            `<option value="${filha.id}">${Formato.texto(filha.nome)}</option>`).join('')
          + '</optgroup>';
      }).join('');
  },

  quemEQuando() {
    const d = Detalhe.dados;
    return Detalhe.par('Lançado por', d.autor
        ? Formato.texto(d.autor.nome)
        : '<i class="det-fraco">usuário removido</i>')
      + Detalhe.par('Cadastrado em', Detalhe.instante(d.criadoEm))
      + (d.estabelecimento
        ? Detalhe.par('Estabelecimento',
          `${Formato.texto(d.estabelecimento)}
           <i class="det-fraco">texto bruto da captura</i>`)
        : '');
  },

  instante(bruto) {
    const quando = new Date(bruto);
    return `${quando.toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' })}
      às ${quando.toLocaleTimeString('pt-BR', {
        timeZone: 'America/Sao_Paulo', hour: '2-digit', minute: '2-digit',
      })}`;
  },

  ligacoes() {
    const d = Detalhe.dados;
    const linhas = [];

    if (d.transferencia) {
      linhas.push(Detalhe.par('O outro lado',
        `<button class="chip-mais" type="button" data-detalhe="ir"
           data-id="${d.transferencia.outroLadoId}">abrir #${d.transferencia.outroLadoId} →</button>`));
    }
    if (d.estornoDeId) {
      linhas.push(Detalhe.par('Estorna',
        `<button class="chip-mais" type="button" data-detalhe="ir"
           data-id="${d.estornoDeId}">abrir #${d.estornoDeId} →</button>`));
    }
    if (d.estornadoPorId) {
      linhas.push(Detalhe.par('Estornado por',
        `<button class="chip-mais" type="button" data-detalhe="ir"
           data-id="${d.estornadoPorId}">abrir #${d.estornadoPorId} →</button>`));
    }
    return linhas.length ? Detalhe.bloco('Ligações', linhas.join('')) : '';
  },

  serieEFatura() {
    return `
      <section class="det-bloco">
        <h4>Série e fatura <span class="tele">AINDA NÃO EXISTE</span></h4>
        <div class="emespera">
          <p>Quando o cartão entrar, é aqui que vai dizer <b>de qual fatura</b> este lançamento
            é e em que <b>mês</b> ele conta. E, se for parcela, <b>qual de quantas</b> — com o
            valor da compra inteira.</p>
          <div class="falta">FALTA PARA ISSO EXISTIR: <b>fatura</b> · <b>parcelamento</b></div>
        </div>
      </section>`;
  },

  oQueJaAconteceu() {
    const linhas = Detalhe.historico.map((evento) => {
      const dados = evento.dados || {};
      const frase = Diario.FRASES[evento.tipo];
      return `
        <div class="det-evento${evento.origem === 'SISTEMA' ? ' do-sistema' : ''}">
          <span class="det-quando">${Formato.dia(evento.dia)}
            <i>${Diario.hora(evento.instante)}</i></span>
          <span class="det-frase">${frase ? frase(dados) : Formato.texto(evento.tipo)}</span>
          <span class="det-autor">${evento.origem === 'SISTEMA'
            ? 'A ROTINA' : Formato.texto(evento.autor || '—')}</span>
        </div>`;
    }).join('');

    return `
      <section class="det-bloco">
        <h4>O que já aconteceu com ele</h4>
        ${linhas || '<div class="vazio">Nenhum evento registrado para este lançamento.</div>'}
      </section>`;
  },

  acoes() {
    const d = Detalhe.dados;
    if (d.doCiclo) {
      return `<section class="det-acoes">
          <span class="tele">ESTE LANÇAMENTO É DO SISTEMA — O SALDO DE ABERTURA SE CORRIGE
            EDITANDO O VALOR, E O RESTO NÃO SE MEXE</span>
        </section>`;
    }
    if (Detalhe.confirmando) {
      return `<section class="det-acoes">
          <span class="tele" style="color:var(--pink)">${Extrato.impacto(Detalhe.paraImpacto())}</span>
          <button class="btn sm danger" type="button" data-detalhe="excluir">Confirmar</button>
          <button class="btn sm ghost" type="button" data-detalhe="cancelar">Cancelar</button>
        </section>`;
    }
    return `<section class="det-acoes">
        ${d.estornoDeId ? '' : '<button class="btn sm ghost" type="button" data-detalhe="estornar">Estornar</button>'}
        <button class="btn sm danger" type="button" data-detalhe="confirmar">Excluir</button>
      </section>`;
  },

  paraImpacto() {
    const d = Detalhe.dados;
    return {
      contaId: d.conta ? d.conta.id : null,
      situacao: d.situacao,
      sentido: d.sentido,
      valor: d.valor,
      transferenciaId: d.transferencia ? d.transferencia.id : null,
    };
  },

  async categorizar(categoriaId) {
    await Detalhe.tentar(
      () => API.editarLancamento(Contexto.ambiente.id, Detalhe.id, { categoriaId }),
      () => Detalhe.abrir(Detalhe.id));
  },

  async estornar() {
    await Detalhe.tentar(
      () => API.estornarLancamento(Contexto.ambiente.id, Detalhe.id,
        { dataEvento: Formato.hoje() }),
      () => Detalhe.abrir(Detalhe.id));
  },

  async excluir() {
    await Detalhe.tentar(
      () => API.excluirLancamento(Contexto.ambiente.id, Detalhe.id),
      () => Detalhe.fechar());
  },

  async tentar(acao, aoDarCerto) {
    try {
      await acao();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      document.getElementById('avisoDetalhe').innerHTML =
        `<div class="aviso err">${Formato.texto(erro.paraGente())}</div>`;
      return;
    }
    await Extrato.recarregarTudo();
    await aoDarCerto();
  },
};
