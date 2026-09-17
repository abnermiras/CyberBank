const Home = {
  MAXIMO_DE_PENDENCIAS: 5,

  resumo: null,
  arvore: [],
  ligado: false,

  async montar() {
    Home.ligar();

    let pendencias;
    let categorias;
    try {
      [Home.resumo, pendencias, categorias] = await Promise.all([
        API.resumoDoMes(Contexto.ambiente.id),
        API.extrato(Contexto.ambiente.id, { pendentes: true, limite: Home.MAXIMO_DE_PENDENCIAS }),
        API.arvoreDeCategorias(Contexto.ambiente.id),
      ]);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      document.getElementById('avisoHome').innerHTML =
        `<div class="aviso err">${Formato.texto(erro.paraGente())}</div>`;
      return;
    }

    Home.arvore = categorias.itens;
    document.getElementById('avisoHome').innerHTML = '';
    Home.pintarCabecalho();
    Home.pintarNumeros();
    Home.pintarGasto();
    Home.pintarPendencias(pendencias.itens);
    Home.pintarHorizonte();
    Home.pintarMes();
  },

  pintarCabecalho() {
    const resumo = Home.resumo;
    const [ano, mes] = resumo.mes.split('-');
    const nome = new Date(Number(ano), Number(mes) - 1, 1)
      .toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });

    document.getElementById('mesRef').textContent = nome.toUpperCase();
    document.getElementById('fimDoMes').textContent = Formato.dia(resumo.ultimoDia).slice(0, 5);
    document.getElementById('horizonteFim').textContent = Formato.dia(resumo.ultimoDia);
    document.getElementById('statusLinha').textContent =
      `NÓ ONLINE · ${Home.diasQueFaltam()}`;
    document.getElementById('regressiva').textContent = Home.diasQueFaltam();
  },

  diasQueFaltam() {
    const dias = Home.resumo.diasAteOFimDoMes;
    if (dias === 0) return 'ÚLTIMO DIA DO MÊS';
    return `T−${dias} DIA${dias > 1 ? 'S' : ''} ATÉ O FIM DO MÊS`;
  },

  pintarNumeros() {
    const resumo = Home.resumo;
    Home.numero('mEmCaixa', resumo.emCaixaCentavos);
    Home.numero('mSobra', resumo.sobraAteOFimDoMesCentavos);
    Home.numero('mGuardado', resumo.guardadoCentavos);
    Home.numero('mPatrimonio', resumo.patrimonioCentavos);

    document.getElementById('contaDaSobra').innerHTML =
      `${Formato.dinheiro(resumo.emCaixaCentavos)} + ${Formato.dinheiro(resumo.aReceberCentavos)}`
      + ` − ${Formato.dinheiro(resumo.aPagarCentavos)}`;
  },

  numero(id, centavos) {
    const campo = document.getElementById(id);
    campo.textContent = Formato.dinheiro(centavos);
    campo.classList.toggle('neg', centavos < 0);
    campo.closest('.metric').classList.remove('semdado');
  },

  pintarGasto() {
    const linhas = Home.resumo.gastoPorCategoria;
    const total = linhas.reduce((soma, linha) => soma + linha.totalCentavos, 0);
    const teto = linhas.length ? linhas[0].totalCentavos : 1;

    document.getElementById('gastoDoMes').textContent = Formato.dinheiro(total);

    document.getElementById('catList').innerHTML = linhas.length
      ? linhas.map((linha) => Home.barraDeCategoria(linha, teto, total)).join('')
      : `<div class="vazio">O mês ainda não teve gasto.<br>
           <b>Lançar</b> é a tecla <b>N</b>, em qualquer tela.</div>`;

    const guardado = Home.resumo.guardadoNoMesCentavos;
    document.getElementById('guardadoLinha').innerHTML = `
      <div class="guardadoline${guardado ? '' : ' vazia'}">
        <div>
          <span class="t">Guardado no mês</span>
          <small>NÃO É GASTO — POR ISSO NÃO ESTÁ NA LISTA ACIMA</small>
        </div>
        <span class="v">${Formato.dinheiro(guardado)}</span>
      </div>`;
  },

  barraDeCategoria(linha, teto, total) {
    const semCategoria = linha.categoriaId === null;
    const tom = semCategoria ? 'var(--muted)' : Formato.tom(linha.cor);
    const fatia = total ? Math.round((linha.totalCentavos / total) * 100) : 0;
    const largura = Math.max(3, (linha.totalCentavos / teto) * 100);

    return `
      <div class="catrow${semCategoria ? ' sem' : ''}" style="--k:${tom}">
        <span class="nome">${semCategoria ? 'Sem categoria' : Formato.texto(linha.nome)}</span>
        <span class="vlr">${Formato.dinheiro(linha.totalCentavos)}</span>
        <span class="bar"><i style="width:${largura}%"></i></span>
        <span class="meta">
          <span>${linha.lancamentos} LANÇAMENTO${linha.lancamentos > 1 ? 'S' : ''}</span>
          <span>${fatia}% DO MÊS</span>
        </span>
      </div>`;
  },

  pintarPendencias(itens) {
    const total = Home.resumo.pendencias;
    document.getElementById('contagemPendencias').innerHTML = total
      ? `<b>${itens.length}</b> DE ${total} SEM CATEGORIA`
      : 'TUDO CATEGORIZADO';

    document.getElementById('pendList').innerHTML = itens.length
      ? itens.map((lancamento) => Home.linhaPendente(lancamento)).join('')
        + (total > itens.length
          ? `<a class="chip-mais" href="#/extrato">VER AS ${total} NO EXTRATO →</a>`
          : '')
      : `<div class="vazio">Nada pendente.<br><b>Todo lançamento tem categoria.</b></div>`;
  },

  linhaPendente(lancamento) {
    const sinal = lancamento.sentido === 'SAIDA' ? '−' : '+';
    return `
      <div class="pendrow">
        <div class="l">
          <div class="desc">${Formato.texto(lancamento.descricao)}</div>
          <div class="sub2">${Formato.dia(lancamento.dataEvento)} ·
            <b class="${lancamento.sentido === 'SAIDA' ? 'neg' : 'pos'}"
              >${sinal} ${Formato.dinheiro(lancamento.valor)}</b></div>
        </div>
        <select class="filtro" data-pendente="${lancamento.id}"
                aria-label="Categoria de ${Formato.texto(lancamento.descricao)}">
          ${Home.opcoesDoSentido(lancamento.sentido)}
        </select>
      </div>`;
  },

  opcoesDoSentido(sentido) {
    const raizes = Home.arvore
      .filter((raiz) => !raiz.sistema && raiz.sentido === sentido && !raiz.inativa);

    if (!raizes.length) return '<option value="">— sem categoria do sentido —</option>';

    return '<option value="">— escolher —</option>'
      + raizes.filter((raiz) => raiz.escolhivel)
        .map((raiz) => `<option value="${raiz.id}">${Formato.texto(raiz.nome)}</option>`)
        .join('')
      + raizes.filter((raiz) => !raiz.escolhivel).map((raiz) => {
        const filhas = raiz.filhas.filter((filha) => filha.escolhivel);
        if (!filhas.length) return '';
        return `<optgroup label="${Formato.texto(raiz.nome)}">`
          + filhas.map((filha) =>
            `<option value="${filha.id}">${Formato.texto(filha.nome)}</option>`).join('')
          + '</optgroup>';
      }).join('');
  },

  pintarHorizonte() {
    const resumo = Home.resumo;
    document.getElementById('horizonteTotais').innerHTML = `
      <div class="htotal pagar">
        <span class="lbl">A pagar</span>
        <span class="v">${Formato.dinheiro(resumo.aPagarCentavos)}</span>
      </div>
      <div class="htotal receber">
        <span class="lbl">A receber</span>
        <span class="v">${Formato.dinheiro(resumo.aReceberCentavos)}</span>
      </div>`;

    document.getElementById('proximosList').innerHTML = resumo.proximos.length
      ? resumo.proximos.map((previsto) => Home.linhaPrevista(previsto)).join('')
      : `<div class="vazio">Nada previsto até o fim do mês.<br>
           <b>Boleto com vencimento</b> aparece aqui assim que for lançado.</div>`;
  },

  linhaPrevista(previsto) {
    const saida = previsto.sentido === 'SAIDA';
    const dias = Home.diasAte(previsto.dataEfeito);
    const quando = dias === 0 ? 'HOJE' : `T−${dias}`;

    return `
      <a class="prevrow" href="#/extrato">
        <span class="dia">${Formato.dia(previsto.dataEfeito).slice(0, 5)}</span>
        <span class="quando${dias === 0 ? ' hoje' : ''}">${quando}</span>
        <span class="desc">${Formato.texto(previsto.descricao)}</span>
        <span class="v ${saida ? 'neg' : 'pos'}">${saida ? '−' : '+'} ${Formato.dinheiro(previsto.valorCentavos)}</span>
      </a>`;
  },

  diasAte(dia) {
    const destino = Date.parse(`${dia}T12:00:00Z`);
    const hoje = Date.parse(`${Home.resumo.hoje}T12:00:00Z`);
    return Math.max(0, Math.round((destino - hoje) / 86400000));
  },

  pintarMes() {
    const resumo = Home.resumo;
    const teto = Math.max(resumo.entrouNoMesCentavos, resumo.saiuNoMesCentavos, 1);
    const sobrou = resumo.entrouNoMesCentavos - resumo.saiuNoMesCentavos;

    document.getElementById('mesEmNumeros').innerHTML = `
      ${Home.linhaDoMes('Entrou', resumo.entrouNoMesCentavos, teto, 'pos')}
      ${Home.linhaDoMes('Saiu', resumo.saiuNoMesCentavos, teto, 'neg')}
      ${Home.linhaDoMes('Guardou', resumo.guardadoNoMesCentavos, teto, 'guardou')}
      <div class="mes-saldo">
        <span class="tele">ENTROU MENOS SAIU</span>
        <span class="v ${sobrou < 0 ? 'neg' : 'pos'}">${Formato.dinheiro(sobrou)}</span>
      </div>
      <p class="rodape-regra">É o que o mês fez até agora — <b>não é projeção</b>. O que ainda
        vai cair está no painel ao lado.</p>`;
  },

  linhaDoMes(rotulo, centavos, teto, tom) {
    return `
      <div class="mesrow">
        <span class="lbl">${rotulo}</span>
        <span class="v ${tom}">${Formato.dinheiro(centavos)}</span>
        <span class="bar ${tom}"><i style="width:${Math.max(2, (centavos / teto) * 100)}%"></i></span>
      </div>`;
  },

  ligar() {
    if (Home.ligado) return;
    Home.ligado = true;

    document.getElementById('pendList').addEventListener('change', async (evento) => {
      const seletor = evento.target.closest('[data-pendente]');
      if (!seletor || !seletor.value) return;

      seletor.disabled = true;
      try {
        await API.editarLancamento(Contexto.ambiente.id, seletor.dataset.pendente,
          { categoriaId: Number(seletor.value) });
      } catch (erro) {
        seletor.disabled = false;
        if (tratarFalha(erro)) return;
        document.getElementById('avisoHome').innerHTML =
          `<div class="aviso err">${Formato.texto(erro.paraGente())}</div>`;
        return;
      }
      await Home.montar();
    });
  },
};
