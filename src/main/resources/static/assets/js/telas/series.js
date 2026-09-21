const Series = {
  dados: null,
  contas: [],
  meios: [],

  async montar() {
    try {
      const [series, contas, meios] = await Promise.all([
        API.series(Contexto.ambiente.id),
        API.listarContas(Contexto.ambiente.id),
        API.listarMeios(Contexto.ambiente.id),
      ]);
      Series.dados = series;
      Series.contas = contas.itens;
      Series.meios = meios.itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      document.getElementById('seriesCorpo').innerHTML =
        `<div class="aviso err">${Formato.texto(erro.paraGente())}</div>`;
      return;
    }
    Series.desenhar();
  },

  desenhar() {
    const recorrencias = Series.dados.recorrencias;
    const parcelamentos = Series.dados.parcelamentos;
    const ativas = recorrencias.filter((r) => r.ativa);
    const emAberto = parcelamentos.filter((p) => p.restanteCentavos > 0);

    const porMes = ativas.reduce((soma, r) => soma + r.valorCentavos, 0);
    const restante = emAberto.reduce((soma, p) => soma + p.restanteCentavos, 0);

    document.getElementById('seriesCorpo').innerHTML = `
      <div class="grid3">
        <div class="panel metric">
          <span class="lbl">Assinaturas por mês</span>
          <span class="val ac">${Formato.dinheiro(porMes)}</span>
          <span class="sub">${Series.contagem(ativas.length, 'ATIVA', 'ATIVAS')} · NÃO ACABA</span>
        </div>
        <div class="panel metric">
          <span class="lbl">Parcelas a vencer</span>
          <span class="val cy">${Formato.dinheiro(restante)}</span>
          <span class="sub">${Series.contagem(emAberto.length, 'COMPRA EM ABERTO',
            'COMPRAS EM ABERTO')} · ACABA</span>
        </div>
        <div class="panel metric">
          <span class="lbl">Comprometido no mês</span>
          <span class="val">${Formato.dinheiro(porMes + Series.parcelaDoMes(emAberto))}</span>
          <span class="sub">ASSINATURAS MAIS A PARCELA DE CADA COMPRA</span>
        </div>
      </div>

      <div class="grupo-de-series">
        <div class="eyebrow">Recorrências // o que não acaba</div>
        <span class="contagem">${Series.contagem(recorrencias.length, 'SÉRIE', 'SÉRIES')}</span>
      </div>
      ${recorrencias.length
        ? `<div class="seriegrid">${recorrencias.map(Series.cartaoDaRecorrencia).join('')}</div>`
        : `<div class="vazio">Nenhuma assinatura ainda. No <b>+</b>, aba <b>CRÉDITO</b>,
            escolha <b>RECORRENTE</b> e diga o dia do mês — é lá que se cadastra a Netflix,
            e não aqui.</div>`}
      <p class="rodape-regra">Recorrência são <b>N eventos independentes</b> sem fim. Junho a
        R$ 39,90 e julho a R$ 55,00 são <b>dois fatos verdadeiros</b>, e é por isso que ela não
        tem valor total. Só existe a ocorrência do <b>ciclo aberto</b>: cada fechamento de
        fatura lança a do ciclo seguinte, uma por vez.</p>

      <div class="grupo-de-series parcelada">
        <div class="eyebrow">Parcelamentos // o que acaba</div>
        <span class="contagem">${Series.contagem(parcelamentos.length, 'COMPRA', 'COMPRAS')}</span>
      </div>
      ${parcelamentos.length
        ? `<div class="seriegrid">${parcelamentos.map(Series.cartaoDoParcelamento).join('')}</div>`
        : `<div class="vazio">Nenhuma compra parcelada. No <b>+</b>, aba <b>CRÉDITO</b>,
            escolha <b>PARCELADO</b> e diga em quantas vezes.</div>`}
      <p class="rodape-regra">Parcelamento é <b>uma compra só</b>, dividida em N. R$ 5.000 em
        10x continua sendo R$ 5.000 — as parcelas nascem <b>todas juntas</b> e provisionadas na
        data da compra, cada uma na fatura do seu mês. Quando a loja estorna, ela devolve o
        <b>total de uma vez</b> e as parcelas seguem correndo.</p>

      <section class="panel mt28">
        <div class="panel-head"><h3>O que ainda não dá para fazer aqui</h3></div>
        <p class="rodape-regra"><b>Alterar e cancelar série</b> ainda não existem nesta tela —
          por isso nenhum cartão tem botão. Quando existirem, as duas não vão se comportar
          igual, e as etiquetas no alto de cada cartão já dizem qual é qual: parcelamento
          <b>altera todas</b> as parcelas e não pergunta; recorrência <b>pergunta</b> se muda
          só o futuro ou o passado também. Nenhuma fatura precisa ser reaberta para isso —
          fatura fechada não congela nada. O que falta é mostrar <b>quais</b> faturas mudam de
          valor antes de confirmar.</p>
      </section>`;
  },

  cartaoDaRecorrencia(r) {
    const encerrada = !r.ativa;

    return `
      <article class="panel serie${encerrada ? ' encerrada' : ''}">
        <span class="corner tr"></span>
        <div class="hstack between gap10">
          <span class="serie-tipo">Recorrência</span>
          ${encerrada
            ? '<span class="tag inativa">CANCELADA</span>'
            : '<span class="tag prev">PERGUNTA AO EDITAR</span>'}
        </div>

        <div class="serie-nome">${Formato.texto(r.descricao)}</div>

        <div class="metric">
          <span class="lbl">Valor por ocorrência</span>
          <span class="val ac">${Formato.dinheiro(r.valorCentavos)}</span>
          <span class="sub">TODO DIA ${r.dia} · ${encerrada ? 'DESLIGADA' : 'SEM DATA DE FIM'}</span>
        </div>

        <div class="serie-dados">
          ${Formato.texto(Series.rotuloDoMeio(r.meioId))}<br>
          ATIVA DESDE <b>${Formato.dia(r.inicio)}</b> ·
          <b>${r.ocorrenciasLancadas}</b> ${r.ocorrenciasLancadas === 1 ? 'COBRANÇA' : 'COBRANÇAS'} ·
          JÁ COBRADO <b>${Formato.dinheiro(r.jaCobradoCentavos)}</b>
        </div>
        ${r.proximaCobrancaEm
          ? `<div class="serie-proxima">▸ PRÓXIMA COBRANÇA ${Formato.dia(r.proximaCobrancaEm)}
             · AINDA NÃO COBRADA</div>`
          : '<div class="serie-dados">SEM OCORRÊNCIA NO CICLO ABERTO</div>'}
      </article>`;
  },

  cartaoDoParcelamento(p) {
    const quitado = p.restanteCentavos <= 0;
    const faltam = p.parcelas - p.parcelasLiquidadas;
    const porParcela = Math.round(p.valorDaCompraCentavos / p.parcelas);
    const andamento = Math.round((p.parcelasLiquidadas / p.parcelas) * 100);

    return `
      <article class="panel serie parcelada${quitado ? ' encerrada' : ''}">
        <span class="corner tr"></span>
        <div class="hstack between gap10">
          <span class="serie-tipo">Parcelamento</span>
          ${quitado
            ? '<span class="tag real">QUITADA</span>'
            : '<span class="tag transf">ALTERA TODAS</span>'}
        </div>

        <div class="serie-nome">${Formato.texto(p.descricao)}</div>

        <div class="metric">
          <span class="lbl">Valor total da compra</span>
          <span class="val cy">${Formato.dinheiro(p.valorDaCompraCentavos)}</span>
          <span class="sub">${p.parcelas}X DE ${Formato.dinheiro(porParcela)}</span>
        </div>

        <div class="bar"><i style="width:${andamento}%"></i></div>

        <div class="serie-dados">
          <b>${p.parcelasLiquidadas}</b> DE <b>${p.parcelas}</b> PAGAS ·
          ${quitado
            ? 'NADA A VENCER'
            : `FALTAM <b>${faltam}</b>, DE <b>${Formato.dinheiro(p.restanteCentavos)}</b>`}<br>
          ${Formato.texto(Series.rotuloDoMeio(p.meioId))} ·
          COMPRA DE <b>${Formato.dia(p.dataDaCompra)}</b>
        </div>
        ${p.proximaParcelaEm && !quitado
          ? `<div class="serie-proxima">▸ PRÓXIMA PARCELA ${Formato.dia(p.proximaParcelaEm)}</div>`
          : ''}
      </article>`;
  },

  parcelaDoMes(parcelamentos) {
    return parcelamentos.reduce((soma, p) =>
      soma + Math.round(p.valorDaCompraCentavos / p.parcelas), 0);
  },

  rotuloDoMeio(meioId) {
    const meio = Series.meios.find((m) => m.id === meioId);
    return meio ? Contas.identidadeDoMeio(meio, Series.contas) : '—';
  },

  contagem(quantos, singular, plural) {
    return quantos === 1 ? `1 ${singular}` : `${quantos} ${plural}`;
  },
};
