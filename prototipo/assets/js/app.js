/* =========================================================================
   CYBERBANK // UI do terminal
   Decisoes de UX exercitadas aqui (docs/06-interface/navegacao.md):
   - o ambiente ativo vive no header e nunca sai da tela
   - quick-add para o caso comum, com saida para o formulario completo
   - o formulario completo EXPLICA o que o modelo vai fazer antes de fazer
   ========================================================================= */
(function (CB) {
  'use strict';
  const { S, D, M } = CB;
  const $ = (s, r) => (r || document).querySelector(s);
  const $$ = (s, r) => Array.from((r || document).querySelectorAll(s));
  const esc = (t) => String(t == null ? '' : t).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

  let tela = 'home';
  let soPendencias = false;
  let contaFiltro = '';
  let faturaSel = '';

  let toastT;
  function toast(txt, warn) {
    const t = $('#toast');
    t.textContent = txt; t.classList.toggle('warn', !!warn); t.classList.add('show');
    clearTimeout(toastT); toastT = setTimeout(() => t.classList.remove('show'), 2600);
  }

  const params = new URLSearchParams(location.search);
  const quem = (params.get('user') || 'DEMO').toUpperCase().slice(0, 14);
  $('#quem').textContent = quem;
  $('#avatar').textContent = quem[0] || 'D';

  /* ================= HEADER ================= */
  function renderHeader() {
    const a = S.ambientes.find((x) => x.id === S.ambienteAtivo);
    $('#ambNome').textContent = a.nome;
    $('#ambDot').style.background = a.cor;
    $('#ambChip').style.borderColor = a.cor + '66';
    $('#hoje').textContent = D.br(S.hoje);
    $('#ambMenu').innerHTML = '<div class="sep">TROCAR DE AMBIENTE</div>' + S.ambientes.map((x) =>
      '<button data-amb="' + x.id + '"><span class="dot" style="width:9px;height:9px;background:' + x.cor + ';box-shadow:0 0 10px ' + x.cor + '"></span>' +
      '<span>' + esc(x.nome) + '<small>SETOR ' + esc(x.setor) + ' &middot; ' + (x.id === S.ambienteAtivo ? 'ATIVO' : 'ACESSO: DONO') + '</small></span></button>').join('');
  }

  $('#ambChip').addEventListener('click', (e) => {
    const b = e.target.closest('[data-amb]');
    if (b) {
      S.ambienteAtivo = b.dataset.amb; contaFiltro = ''; soPendencias = false; faturaSel = '';
      $('#ambMenu').classList.remove('open');
      CB.registrar('ambiente trocado para ' + S.ambientes.find((x) => x.id === S.ambienteAtivo).nome, 'sys');
      renderTudo(); toast('CONTEXTO TROCADO // ' + $('#ambNome').textContent);
      return;
    }
    $('#ambMenu').classList.toggle('open');
  });
  document.addEventListener('click', (e) => { if (!e.target.closest('#ambChip')) $('#ambMenu').classList.remove('open'); });

  $$('[data-adv]').forEach((b) => b.addEventListener('click', () => {
    const ev = CB.avancar(Number(b.dataset.adv));
    renderTudo();
    toast(ev.length ? ev.length + ' EVENTO(S) // ' + D.br(S.hoje) : 'SEM EVENTOS ATÉ ' + D.br(S.hoje));
  }));
  $('#btnReset').addEventListener('click', () => location.reload());

  $$('.nav').forEach((b) => b.addEventListener('click', () => {
    tela = b.dataset.tela;
    $$('.nav').forEach((x) => x.classList.toggle('on', x === b));
    $$('.tela').forEach((s) => s.classList.toggle('hidden', s.dataset.tela !== tela));
    renderTudo();
  }));

  /* ================= HOME ================= */
  function renderHome() {
    $('#mEmCaixa').textContent = M.fmt(CB.emCaixa());
    $('#mSobra').textContent = M.fmt(CB.sobraAteFimDoMes());
    $('#mGuardado').textContent = M.fmt(CB.guardado());
    $('#mPatrimonio').textContent = M.fmt(CB.patrimonio());
    $('#mDivida').textContent = M.fmt(CB.dividaTotal());
    $('#fimMes').textContent = D.br(D.fimDoMes(S.hoje)).slice(0, 5);
    $('#mesRef').textContent = D.parse(S.hoje).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }).toUpperCase();

    const cats = CB.gastoPorCategoria(D.mes(S.hoje));
    const teto = cats.length ? cats[0].total : 1;
    $('#catList').innerHTML = cats.length ? cats.map((c) =>
      '<div class="catrow"><span class="nome" style="color:' + c.cor + '">' + esc(c.nome) + '</span>' +
      '<span class="vlr">' + M.fmt(c.total) + '</span>' +
      '<span class="bar"><i style="width:' + Math.max(4, (c.total / teto) * 100) + '%;background:' + c.cor + '"></i></span>' +
      '<span class="meta">' + c.itens + ' LAN\u00c7AMENTO' + (c.itens > 1 ? 'S' : '') + '</span></div>').join('')
      : '<div class="vazio">SEM GASTO NO MÊS</div>';

    $('#guardadoLine').innerHTML = '<div><span class="t">GUARDADO NO M&Ecirc;S</span>' +
      '<small>N&Atilde;O &Eacute; GASTO — POR ISSO N&Atilde;O EST&Aacute; NA LISTA ACIMA</small></div>' +
      '<span class="v">' + M.fmt(CB.guardadoNoMes(D.mes(S.hoje))) + '</span>';

    const p = CB.pendencias();
    $('#pendCount').textContent = String(p.length).padStart(2, '0');
    $('#pendList').innerHTML = p.length ? p.slice(0, 6).map((l) =>
      '<div class="pendrow"><div class="l"><div class="desc">' + esc(l.descricao) + '</div>' +
      '<div class="sub2">' + D.br(l.dataEvento) + ' &middot; ' + M.fmt(l.valor) + (l.estabelecimento ? ' &middot; ' + esc(l.estabelecimento) : '') + '</div></div>' +
      '<select class="minisel" data-catpend="' + l.id + '">' + optCategorias(l.sentido, '') + '</select></div>').join('')
      : '<div class="vazio">NADA PENDENTE // TUDO CATEGORIZADO</div>';

    // a fatura e recorte da conta CARTAO — o contrato (ADR-0003)
    const ccCartao = CB.contas().find(CB.ehDivida);
    if (!ccCartao) { $('#boxFatura').classList.add('hidden'); }
    else {
      $('#boxFatura').classList.remove('hidden');
      const fs = CB.faturasDe(ccCartao.id);
      const f = fs.find((x) => x.status === 'FECHADA' && CB.situacaoPagamento(x.id) !== 'QUITADA')
        || fs.find((x) => x.status === 'ABERTA') || fs[fs.length - 1];
      if (!f) { $('#fatResumo').innerHTML = '<div class="vazio">SEM FATURA</div>'; $('#fatRef').textContent = '—'; }
      else {
        $('#fatRef').textContent = ccCartao.nome;
        $('#fatResumo').innerHTML =
          '<div class="hstack between"><span class="stbadge st-' + f.status + '">' + f.status + '</span>' +
          '<span class="tele">REF ' + f.referencia + ' &middot; ' + CB.situacaoPagamento(f.id) + '</span></div>' +
          '<div class="metric mt10"><span class="val ac" style="font-size:30px">' + M.fmt(CB.totalFatura(f.id)) + '</span>' +
          '<span class="sub">FECHA ' + D.br(f.fechamento) + ' &middot; VENCE ' + D.br(f.vencimento) + '</span></div>' +
          '<div class="fatacoes">' + acoesFatura(f) + '</div>';
      }
    }

    $('#logBox').innerHTML = S.log.slice(0, 14).map((l) =>
      '<div><span class="t">' + D.br(l.t) + '</span><span class="' + l.tipo + '">' + esc(l.txt) + '</span></div>').join('');
  }

  // ABRIR so aparece na ULTIMA fechada: e a unica em que "a seguinte volta a FUTURA"
  // faz sentido. Para mexer no passado, edita-se o lancamento — nada congela.
  function acoesFatura(f) {
    const b = [];
    if (f.status === 'ABERTA') b.push('<button class="btn sm" data-fat="fechar" data-id="' + f.id + '">FECHAR FATURA</button>');
    if (f.status === 'FECHADA') {
      const falta = CB.totalFatura(f.id) - CB.pagoDaFatura(f.id);
      const pago = CB.pagoDaFatura(f.id);
      if (falta > 0) {
        b.push('<button class="btn sm primary" data-fat="pagar" data-id="' + f.id + '">PAGAR ' + M.fmt(falta) + '</button>');
        b.push('<button class="btn sm" data-fat="parcial" data-id="' + f.id + '">PAGAR PARTE</button>');
      }
      if (pago > 0 && falta !== 0) b.push('<button class="btn sm" data-fat="ajustar" data-id="' + f.id + '">AJUSTAR PAGAMENTO</button>');
    }
    if (CB.podeAbrir(f)) b.push('<button class="btn sm danger" data-fat="abrir" data-id="' + f.id + '">ABRIR</button>');
    return b.join('');
  }

  document.addEventListener('click', (e) => {
    const b = e.target.closest('[data-fat]');
    if (!b) return;
    const acao = b.dataset.fat, fid = b.dataset.id;
    if (acao === 'fechar') { CB.fecharFatura(fid); toast('FECHADA // PRÓXIMA ABERTA · RECORRÊNCIAS LANÇADAS · PAGAMENTO PREVISTO CRIADO'); }
    if (acao === 'pagar') { CB.pagarFatura(fid); toast('PAGA // TRANSFERÊNCIA DA CONTA PAGADORA PARA O CARTÃO'); }
    if (acao === 'parcial') {
      const falta = CB.totalFatura(fid) - CB.pagoDaFatura(fid);
      const txt = prompt('Quanto pagar agora? Falta ' + M.fmt(falta) + '.', (falta / 100).toFixed(2).replace('.', ','));
      if (!txt) return;
      const v = M.parse(txt);
      if (!v || v <= 0) { toast('VALOR INVÁLIDO', true); return; }
      CB.pagarFatura(fid, { valor: v });
      toast('PAGO ' + M.fmt(v) + ' // O RESTO FICA COMO SALDO DO CARTÃO — SEM ROLAGEM');
    }
    if (acao === 'ajustar') { const v = CB.ajustarPagamento(fid); toast(v ? 'PAGAMENTO AJUSTADO PARA ' + M.fmt(v) : 'NADA A AJUSTAR', !v); }
    if (acao === 'abrir') { CB.abrirFatura(fid); toast('ABERTA // A SEGUINTE VOLTOU A FUTURA E MANTEVE O QUE TINHA', true); }
    renderTudo();
  });

  document.addEventListener('change', (e) => {
    const s = e.target.closest('[data-catpend]');
    if (!s || !s.value) return;
    CB.editar(s.dataset.catpend, { categoria: s.value }, quem);
    toast('CATEGORIZADO'); renderTudo();
  });

  /* ================= EXTRATO ================= */
  function renderExtrato() {
    $('#fConta').innerHTML = '<option value="">TODAS AS CONTAS</option>' +
      CB.contas().map((c) => '<option value="' + c.id + '"' + (c.id === contaFiltro ? ' selected' : '') + '>' + esc(c.nome) + '</option>').join('');
    $('#btnSoPend').classList.toggle('primary', soPendencias);
    let ls = CB.extrato(contaFiltro ? { conta: contaFiltro } : null);
    if (soPendencias) ls = ls.filter((l) => !l.categoria && CB.esperaCategoria(l));
    $('#extList').innerHTML = ls.length ? ls.map(itemHTML).join('') : '<div class="vazio">NENHUM LANÇAMENTO</div>';
  }

  function itemHTML(l) {
    const c = CB.conta(l.conta), k = l.categoria ? CB.categoria(l.categoria) : null, r = l.categoria ? CB.raizDe(l.categoria) : null;
    const m = l.meio ? CB.meio(l.meio) : null;
    const tags = ['<span class="tag ' + (l.situacao === 'PREVISTO' ? 'prev' : 'real') + '">' + l.situacao + '</span>'];
    if (l.transferenciaId) tags.push('<span class="tag transf">TRANSFER&Ecirc;NCIA</span>');
    if (l.rendimento) tags.push('<span class="tag transf">RENDIMENTO</span>');
    if (!l.categoria && CB.esperaCategoria(l)) tags.push('<span class="tag pend">SEM CATEGORIA</span>');
    const dif = l.dataEfeito !== l.dataEvento ? ' &middot; EFEITO ' + D.br(l.dataEfeito) : '';
    return '<div class="item"><div class="d"><b>' + l.dataEvento.slice(8, 10) + '</b>' + l.dataEvento.slice(5, 7) + '/' + l.dataEvento.slice(2, 4) + '</div>' +
      '<div><div class="desc">' + esc(l.descricao) + '</div><div class="sub2">' + esc(c ? c.nome : '') +
      (m ? ' &middot; ' + esc(m.nome) : '') + (k ? ' &middot; ' + esc(r.nome) + (k.pai ? ' &rsaquo; ' + esc(k.nome) : '') : '') + dif + '</div>' +
      '<div class="sub2">' + tags.join('') + '</div></div>' +
      '<div class="amt ' + (l.sentido === 'ENTRADA' ? 'pos' : 'neg') + '">' + (l.sentido === 'ENTRADA' ? '+' : '&minus;') + ' ' +
      M.fmt(l.valor).replace('R$ ', '') + '</div></div>';
  }

  $('#fConta').addEventListener('change', (e) => { contaFiltro = e.target.value; renderExtrato(); });
  $('#btnSoPend').addEventListener('click', () => { soPendencias = !soPendencias; renderExtrato(); });

  /* ================= FATURA (tela) ================= */
  function renderFatura() {
    const fs = CB.faturas().sort((a, b) => b.referencia.localeCompare(a.referencia));
    if (!fs.length) { $('#fatDetalhe').innerHTML = '<div class="panel vazio">ESTE AMBIENTE NÃO TEM CARTÃO DE CRÉDITO</div>'; $('#fSel').innerHTML = ''; return; }
    // a selecao vive numa variavel, nao no DOM: o <select> auto-seleciona a primeira
    // opcao ao ser repopulado e sequestrava a escolha padrao
    if (!faturaSel || !fs.some((f) => f.id === faturaSel)) {
      const padrao = fs.find((x) => x.status === 'FECHADA') || fs.find((x) => x.status === 'ABERTA') || fs[0];
      faturaSel = padrao.id;
    }
    $('#fSel').innerHTML = fs.map((f) => '<option value="' + f.id + '"' + (f.id === faturaSel ? ' selected' : '') + '>' +
      esc(CB.conta(f.contaCartao).nome) + ' &middot; ' + f.referencia + '</option>').join('');
    const f = fs.find((x) => x.id === faturaSel);
    const cc = CB.conta(f.contaCartao);
    const ls = CB.lancamentosDaFatura(f.id);
    const pago = CB.pagoDaFatura(f.id), falta = CB.totalFatura(f.id) - pago;
    // NADA CONGELA: a explicacao mudou de "reabra para editar" para "edite direto"
    const nota = f.status !== 'ABERTA'
      ? '<div class="explica">FATURA ' + f.status + ' // os lan&ccedil;amentos <b>continuam edit&aacute;veis</b> — nada congela. Se ela j&aacute; foi paga, corrigir um valor faz o sistema <b>perguntar</b>: ajustar o pagamento, ou deixar a diferen&ccedil;a como saldo da conta do cart&atilde;o. Pagar &eacute; <b>transfer&ecirc;ncia</b>, e o que n&atilde;o foi pago simplesmente fica no saldo — sem rolagem.</div>' : '';
    $('#fatDetalhe').innerHTML =
      '<div class="fatgrid">' +
      '<div class="panel hot metric"><span class="lbl">Total</span><span class="val ac">' + M.fmt(CB.totalFatura(f.id)) + '</span><span class="sub">' + ls.length + ' LAN\u00c7AMENTOS</span></div>' +
      '<div class="panel metric"><span class="lbl">' + (falta > 0 ? 'Falta pagar' : 'Pago') + '</span><span class="val pk" style="font-size:26px">' + M.fmt(falta > 0 ? falta : pago) + '</span><span class="sub"><span class="stbadge st-' + f.status + '">' + f.status + '</span> &middot; ' + CB.situacaoPagamento(f.id) + '</span></div>' +
      '<div class="panel metric"><span class="lbl">Fechamento</span><span class="val cy" style="font-size:26px">' + D.br(f.fechamento) + '</span><span class="sub">' + cc.diasAntesFechamento + ' DIAS ANTES DO VENCIMENTO</span></div>' +
      '<div class="panel metric"><span class="lbl">Vencimento</span><span class="val pk" style="font-size:26px">' + D.br(f.vencimento) + '</span><span class="sub">DIA ' + cc.diaVencimento + ' &middot; PAGA POR ' + esc((CB.conta(cc.contaPagadora) || {}).apelido || '—') + '</span></div>' +
      '</div>' + limiteHTML(f.contaCartao) + '<div class="panel"><div class="panel-head"><h3>Lançamentos da fatura</h3>' +
      '<div class="fatacoes" style="margin:0">' + acoesFatura(f) + '</div></div>' + nota +
      '<div style="margin:0 -20px">' + (ls.map(itemHTML).join('') || '<div class="vazio">FATURA VAZIA</div>') + '</div></div>';
  }
  $('#fSel').addEventListener('change', (e) => { faturaSel = e.target.value; renderFatura(); });

  // Limite = do CONTRATO (a conta CARTAO). Preso = divida: tudo comprado e nao pago,
  // parcela futura inclusive — e e por isso que recorrencia nao gera previsto.
  // ACHADO 28/08: o saldo PROJETADO nao serve aqui, porque abate o pagamento previsto.
  function limiteHTML(contaCartaoId) {
    const L = CB.limiteDisponivel(contaCartaoId);
    if (!L) return '';
    const pct = Math.min(100, Math.round((L.preso / L.limite) * 100));
    return '<div class="panel mt18" style="margin-bottom:14px">' +
      '<div class="panel-head"><h3>Limite</h3><span class="tele">PARCELA FUTURA SEGURA LIMITE &middot; RECORRÊNCIA NÃO</span></div>' +
      '<div class="hstack between wrap gap14">' +
      '<div class="metric"><span class="lbl">Disponível</span><span class="val lm" style="font-size:30px">' + M.fmt(L.disponivel) + '</span></div>' +
      '<div class="metric" style="text-align:right"><span class="lbl">Preso em compras não pagas</span>' +
      '<span class="val" style="font-size:20px">' + M.fmt(L.preso) + ' <span style="color:var(--dim);font-size:13px">de ' + M.fmt(L.limite) + '</span></span></div>' +
      '</div><div class="bar mt10"><i style="width:' + pct + '%"></i></div></div>';
  }

  /* ================= PATRIMONIO ================= */
  function renderPatrimonio() {
    const cards = CB.contas().map((c) => {
      const s = CB.ehDivida(c) ? -CB.dividaCartao(c.id) : CB.saldoRealizado(c.id);
      const apl = c.tipo === 'APLICACAO';
      const extra = apl
        ? '<div class="mt10 tele">&Uacute;LTIMA ATUALIZA&Ccedil;&Atilde;O: <b>' + (c.ultimaAtualizacao ? D.br(c.ultimaAtualizacao) : 'NUNCA') + '</b></div>' +
          '<div class="row mt10" style="grid-template-columns:1fr auto;gap:8px;align-items:end">' +
          '<div class="field" style="margin:0"><label>informar valor atual</label>' +
          '<input data-vlr="' + c.id + '" inputmode="decimal" placeholder="' + (s / 100).toFixed(2).replace('.', ',') + '"></div>' +
          '<button class="btn sm" data-rend="' + c.id + '">APLICAR</button></div>' +
          '<p class="hint mt10">A diferen&ccedil;a vira um <b>lan&ccedil;amento de rendimento</b> — o saldo continua sendo a soma dos lan&ccedil;amentos.</p>'
        : '';
      return '<div class="panel ' + (apl ? 'hot' : '') + '">' +
        '<div class="hstack between"><span class="cta-tipo">' + c.tipo + '</span>' +
        '<span class="tag ' + (c.entraNoFluxoDeCaixa ? '' : 'real') + '">' + (c.entraNoFluxoDeCaixa ? 'FLUXO DE CAIXA' : 'PATRIMÔNIO') + '</span></div>' +
        '<div class="metric mt10"><span class="lbl">' + esc(c.apelido || '') + '</span>' +
        '<span class="val ' + (apl ? 'lm' : 'cy') + '" style="font-size:28px">' + M.fmt(s) + '</span>' +
        '<span class="sub">' + esc(c.nome) + '</span></div>' + extra + '</div>';
    }).join('');

    $('#patContas').innerHTML = '<div class="ctagrid">' + cards + '</div>' +
      '<div class="panel mt18"><div class="panel-head"><h3>Leituras</h3><span class="tele">FLUXO DE CAIXA &ne; PATRIM&Ocirc;NIO</span></div><div class="grid4">' +
      '<div class="metric"><span class="lbl">Em caixa</span><span class="val cy" style="font-size:26px">' + M.fmt(CB.emCaixa()) + '</span><span class="sub">SÓ CONTAS DE FLUXO</span></div>' +
      '<div class="metric"><span class="lbl">Guardado</span><span class="val lm" style="font-size:26px">' + M.fmt(CB.guardado()) + '</span><span class="sub">FORA DO FLUXO</span></div>' +
      '<div class="metric"><span class="lbl">Patrim&ocirc;nio</span><span class="val" style="font-size:26px">' + M.fmt(CB.patrimonio()) + '</span><span class="sub">TUDO SOMADO, D&Iacute;VIDA DE ' + M.fmt(CB.dividaTotal()) + ' J&Aacute; DESCONTADA</span></div>' +
      '<div class="metric"><span class="lbl">Guardado no m&ecirc;s</span><span class="val ac" style="font-size:26px">' + M.fmt(CB.guardadoNoMes(D.mes(S.hoje))) + '</span><span class="sub">APORTES DO PERÍODO</span></div>' +
      '</div></div>';
  }

  document.addEventListener('click', (e) => {
    const b = e.target.closest('[data-rend]');
    if (!b) return;
    const inp = $('[data-vlr="' + b.dataset.rend + '"]');
    const v = M.parse(inp.value);
    if (!v) { toast('INFORME O VALOR ATUAL', true); return; }
    CB.atualizarValorAplicacao(b.dataset.rend, v);
    toast('VALOR ATUALIZADO // DIFERENÇA LANÇADA'); renderTudo();
  });

  /* ================= SERIES ================= */
  function renderSeries() {
    const ss = CB.series();
    if (!ss.length) { $('#seriesList').innerHTML = '<div class="panel vazio">NENHUMA SÉRIE NESTE AMBIENTE</div>'; return; }
    $('#seriesList').innerHTML = '<div class="ctagrid">' + ss.map((r) => {
      const ls = CB.lancamentosDaSerie(r.id);
      const prev = ls.filter((l) => l.situacao === 'PREVISTO');
      const real = ls.filter((l) => l.situacao === 'REALIZADO');
      const rec = r.tipo === 'RECORRENCIA';
      const proxima = ls.filter((l) => l.dataEvento > S.hoje)
        .sort((a, b) => a.dataEvento.localeCompare(b.dataEvento))[0];
      const travadas = CB.faturasAfetadas(ls);   // agora: faturas fechadas que mudam de valor
      // Recorrencia NAO mostra "N previstas": ela nao tem fim e nao gera futuro.
      // Olha para tras (o que existe de verdade) e para a proxima cobranca, so.
      const gasto = ls.reduce((acc, l) => acc + l.valor, 0);
      const desde = ls.length ? ls.map((l) => l.dataEvento).sort()[0] : null;
      const corpo = rec
        ? '<div class="metric mt10"><span class="lbl">valor por ocorrência</span>' +
          '<span class="val ac" style="font-size:26px">' + M.fmt(r.valor) + '</span>' +
          '<span class="sub">TODO DIA ' + r.dia + ' &middot; SEM DATA DE FIM</span></div>' +
          '<div class="tele mt10">ATIVA DESDE <b>' + (desde ? desde.slice(5, 7) + '/' + desde.slice(0, 4) : '—') +
          '</b> &middot; <b>' + ls.length + '</b> COBRANÇA(S) &middot; <b>' + M.fmt(gasto) + '</b> NO TOTAL</div>' +
          (proxima ? '<div class="tele" style="color:var(--acid)">PRÓXIMA COBRANÇA ' + D.br(proxima.dataEvento) + '</div>' : '')
        : '<div class="metric mt10"><span class="lbl">valor total da compra</span>' +
          '<span class="val cy" style="font-size:26px">' + M.fmt(r.valorTotal) + '</span>' +
          '<span class="sub">' + r.parcelas + 'x DE ' + M.fmt(Math.round(r.valorTotal / r.parcelas)) + '</span></div>' +
          '<div class="tele mt10">' + real.length + ' PAGA(S) &middot; <b>' + prev.length + '</b> A VENCER</div>';
      return '<div class="panel ' + (rec ? '' : 'hot') + '">' +
        '<div class="hstack between"><span class="cta-tipo">' + (rec ? 'RECORRÊNCIA' : 'PARCELAMENTO') + '</span>' +
        '<span class="tag ' + (rec ? 'prev' : 'transf') + '">' + (rec ? 'PERGUNTA AO EDITAR' : 'ALTERA TODAS') + '</span></div>' +
        '<div class="desc mt10" style="font:600 16px var(--disp)">' + esc(r.descricao) + '</div>' + corpo +
        (!rec && travadas.length ? '<div class="tele mt10" style="color:var(--pink)">ALTERAR REABRE ' + travadas.length +
          ' FATURA(S): ' + travadas.map((f) => f.referencia).join(', ') + '</div>' : '') +
        '<div class="fatacoes"><button class="btn sm" data-serie="' + r.id + '">ALTERAR VALOR</button></div></div>';
    }).join('') + '</div>';
  }

  /* ---------- modal de edicao de serie ---------- */
  let smSerie = null, smEscopo = 'FUTURAS';
  function abrirSerie(sid) {
    smSerie = CB.serie(sid); smEscopo = 'FUTURAS';
    const rec = smSerie.tipo === 'RECORRENCIA';
    $('#smTitulo').textContent = rec ? 'Alterar recorrência' : 'Alterar parcelamento';
    $('#smSub').innerHTML = esc(smSerie.descricao) + ' &middot; ' + (rec ? 'RECORRÊNCIA' : 'PARCELAMENTO');
    $('#smLabel').textContent = rec ? 'novo valor por ocorrência' : 'novo valor TOTAL da compra';
    $('#smValor').value = ((rec ? smSerie.valor : smSerie.valorTotal) / 100).toFixed(2).replace('.', ',');
    $('#smEscopo').classList.toggle('hidden', !rec);
    $$('#smSeg button').forEach((b) => b.classList.toggle('on', b.dataset.e === 'FUTURAS'));
    $('#smErr').textContent = '';
    impactoSerie();
    $('#serieM').classList.add('open');
  }
  const fecharSerie = () => { $('#serieM').classList.remove('open'); smSerie = null; };

  // mostra o impacto ANTES de confirmar. Nao ha mais reabertura — o que continua
  // obrigatorio e dizer QUAIS faturas mudam de valor: mexer numa paga nao pode ser
  // efeito colateral silencioso.
  function impactoSerie() {
    if (!smSerie) return;
    const rec = smSerie.tipo === 'RECORRENCIA';
    const ls = CB.lancamentosDaSerie(smSerie.id);
    const alvo = rec ? (smEscopo === 'TODAS' ? ls : ls.filter((l) => l.dataEvento > S.hoje)) : ls;
    const fat = CB.faturasAfetadas(alvo);
    $('#smImpacto').innerHTML = 'VAI ALTERAR <b>' + alvo.length + '</b> LANÇAMENTO(S)' +
      (rec ? (smEscopo === 'TODAS' ? ' &middot; O PASSADO SERÁ REESCRITO' : ' &middot; O PASSADO FICA INTACTO')
           : ' &middot; TODAS AS PARCELAS, SEMPRE — SE DIVERGIREM, O DADO ESTÁ ERRADO') +
      (fat.length ? '<br>MUDA O VALOR DE ' + fat.length + ' FATURA(S) J&Aacute; FECHADA(S): <b>' +
        fat.map((f) => f.referencia + ' [' + CB.situacaoPagamento(f.id) + ']').join(', ') + '</b>' +
        (fat.some((f) => CB.pagoDaFatura(f.id) > 0) ? ' &middot; UMA DELAS J&Aacute; FOI PAGA — VOC&Ecirc; DECIDE SE O PAGAMENTO ACOMPANHA' : '') : '');
  }

  document.addEventListener('click', (e) => {
    const b = e.target.closest('[data-serie]');
    if (b) { abrirSerie(b.dataset.serie); return; }
  });
  $('#smX').addEventListener('click', fecharSerie);
  $('#serieM').addEventListener('click', (e) => { if (e.target === $('#serieM')) fecharSerie(); });
  $('#smSeg').addEventListener('click', (e) => {
    const b = e.target.closest('[data-e]'); if (!b) return;
    smEscopo = b.dataset.e; $$('#smSeg button').forEach((x) => x.classList.toggle('on', x === b)); impactoSerie();
  });
  $('#smOk').addEventListener('click', () => {
    if (!smSerie) return;
    const v = M.parse($('#smValor').value);
    if (!v) { $('#smErr').textContent = 'VALOR INVÁLIDO'; return; }
    try {
      const r = smSerie.tipo === 'RECORRENCIA'
        ? CB.editarRecorrencia(smSerie.id, { valor: v }, smEscopo, quem)
        : CB.editarParcelamento(smSerie.id, v, quem);
      toast(r.alterados + ' LANÇAMENTO(S) ALTERADO(S)' + (r.faturasTocadas ? ' · ' + r.faturasTocadas + ' FATURA FECHADA MUDOU DE VALOR' : ''));
      fecharSerie(); renderTudo();
    } catch (err) { $('#smErr').textContent = String(err.message || err).toUpperCase(); }
  });
  $('#smCancelar').addEventListener('click', () => {
    if (!smSerie) return;
    const r = CB.cancelarSerie(smSerie.id);
    toast(r.removidos + ' PREVISTO(S) REMOVIDO(S) · O PASSADO FICOU INTACTO', true);
    fecharSerie(); renderTudo();
  });

  /* ================= SELECTS ================= */
  function optCategorias(sentido, sel) {
    const raizes = CB.categorias().filter((c) => !c.pai && (!sentido || c.sentido === sentido));
    return '<option value="">— SEM CATEGORIA —</option>' + raizes.map((r) => {
      const fi = CB.categorias().filter((c) => c.pai === r.id);
      const opts = fi.length
        ? fi.map((f) => '<option value="' + f.id + '"' + (f.id === sel ? ' selected' : '') + '>' + esc(f.nome) + '</option>').join('')
        : '<option value="' + r.id + '"' + (r.id === sel ? ' selected' : '') + '>' + esc(r.nome) + '</option>';
      return '<optgroup label="' + esc(r.nome) + '">' + opts + '</optgroup>';
    }).join('');
  }
  const optMeios = (filtro) => CB.meios().filter(filtro || (() => true))
    .map((m) => '<option value="' + m.id + '">' + esc(m.nome) + ' &middot; ' + m.tipo + '</option>').join('');
  const optContas = () => CB.contas().map((c) => '<option value="' + c.id + '">' + esc(c.nome) + '</option>').join('');

  /* ================= QUICK-ADD ================= */
  let qSent = 'SAIDA';
  const abrirQA = () => { $('#qa').classList.add('open'); montarQA(); setTimeout(() => $('#qValor').focus(), 40); };
  const fecharQA = () => { $('#qa').classList.remove('open'); $('#fQuick').reset(); $('#qErr').textContent = ''; };

  function montarQA() {
    $('#qMeio').innerHTML = optMeios(qSent === 'ENTRADA' ? (m) => m.tipo !== 'CREDITO' : null);
    $('#qCat').innerHTML = optCategorias(qSent, '');
  }
  $('#fab').addEventListener('click', abrirQA);
  $('#qa').addEventListener('click', (e) => { if (e.target === $('#qa')) fecharQA(); });
  $('#qaSent').addEventListener('click', (e) => {
    const b = e.target.closest('[data-s]'); if (!b) return;
    qSent = b.dataset.s; $$('#qaSent button').forEach((x) => x.classList.toggle('on', x === b)); montarQA();
  });

  $('#fQuick').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = M.parse($('#qValor').value);
    if (!v) { $('#qErr').textContent = 'VALOR INVÁLIDO'; return; }
    const mid = $('#qMeio').value, m = CB.meio(mid), cat = $('#qCat').value || null, desc = $('#qDesc').value.trim();
    if (m.tipo === 'CREDITO') {
      CB.comprarNoCredito({ cartao: mid, valor: v, data: S.hoje, descricao: desc, categoria: cat });
      toast('NO CRÉDITO // CAIU NA FATURA, NÃO NO SALDO DE HOJE');
    } else {
      CB.lancar({ conta: m.conta, sentido: qSent, valor: v, descricao: desc, categoria: cat, meio: mid, autor: quem });
      toast(cat ? 'LANÇADO' : 'LANÇADO // FOI PARA AS PENDÊNCIAS');
    }
    CB.registrar((qSent === 'SAIDA' ? 'saída' : 'entrada') + ' ' + M.fmt(v) + ' — ' + desc, 'novo');
    fecharQA(); renderTudo();
  });
  $('#qFull').addEventListener('click', () => { fecharQA(); abrirFull(); });

  /* ================= FORM COMPLETO ================= */
  let xTipo = 'gasto';
  const abrirFull = () => { $('#full').classList.add('open'); $('#xData').value = S.hoje; montarFull(); };
  const fecharFull = () => { $('#full').classList.remove('open'); $('#fFull').reset(); $('#xErr').textContent = ''; };
  $('#fullX').addEventListener('click', fecharFull);
  $('#full').addEventListener('click', (e) => { if (e.target === $('#full')) fecharFull(); });
  $('#fTipo').addEventListener('click', (e) => {
    const b = e.target.closest('[data-t]'); if (!b) return;
    xTipo = b.dataset.t; $$('#fTipo button').forEach((x) => x.classList.toggle('on', x === b)); montarFull();
  });

  function montarFull() {
    const mostra = (id, v) => $(id).classList.toggle('hidden', !v);
    mostra('#rowContaMeio', xTipo === 'gasto' || xTipo === 'receita' || xTipo === 'boleto');
    mostra('#rowTransf', xTipo === 'transf');
    mostra('#rowCredito', xTipo === 'credito');
    mostra('#rowVenc', xTipo === 'boleto');

    if (xTipo === 'gasto' || xTipo === 'boleto') {
      $('#xMeio').innerHTML = optMeios(xTipo === 'boleto' ? (m) => m.tipo === 'BOLETO' : (m) => m.tipo !== 'CREDITO');
      $('#xCat').innerHTML = optCategorias('SAIDA', '');
    }
    if (xTipo === 'receita') { $('#xMeio').innerHTML = optMeios((m) => m.tipo !== 'CREDITO'); $('#xCat').innerHTML = optCategorias('ENTRADA', ''); }
    if (xTipo === 'credito') {
      $('#xCartao').innerHTML = optMeios((m) => m.tipo === 'CREDITO');
      let o = ''; for (let i = 1; i <= 12; i++) o += '<option value="' + i + '">' + i + 'x</option>';
      $('#xParc').innerHTML = o;
      $('#xCat').innerHTML = optCategorias('SAIDA', '');
    }
    if (xTipo === 'transf') { $('#xDe').innerHTML = optContas(); $('#xPara').innerHTML = optContas(); }
    if (xTipo === 'boleto') $('#xVenc').value = D.addDias(S.hoje, 10);
    explicar();
  }

  // o formulario diz o que o MODELO vai fazer — o prototipo ensina a regra
  function explicar() {
    const e = $('#xExplica');
    if (xTipo === 'credito') {
      const cid = $('#xCartao').value, n = Number($('#xParc').value || 1), dt = $('#xData').value || S.hoje;
      if (!cid) { e.innerHTML = 'NENHUM CART&Atilde;O NESTE AMBIENTE'; return; }
      const f = CB.faturaAberta(CB.meio(cid).conta);
      e.innerHTML = 'CAI NA FATURA <b>' + f.referencia + '</b> — a <b>ABERTA</b>, pelo STATUS e n&atilde;o pela data' +
        ' &middot; DEBITA A CONTA <b>' + esc(CB.conta(CB.meio(cid).conta).nome) + '</b> HOJE MESMO, COMO <b>REALIZADO</b>' +
        (n > 1 ? ' &middot; ' + n + ' PARCELAS: AS SEGUINTES NASCEM <b>PREVISTAS</b> NAS FATURAS &Agrave; FRENTE' : '');
    } else if (xTipo === 'transf') {
      const para = CB.conta($('#xPara').value);
      e.innerHTML = 'CRIA <b>DOIS LAN&Ccedil;AMENTOS</b> LIGADOS PELO MESMO ID &middot; SEM CATEGORIA &middot; N&Atilde;O ENTRA NO RELAT&Oacute;RIO DE GASTO' +
        (para && !para.entraNoFluxoDeCaixa ? ' &middot; &Eacute; UM <b>APORTE</b>: SAI DO FLUXO DE CAIXA E ENTRA NO PATRIM&Ocirc;NIO' : '');
    } else if (xTipo === 'boleto') {
      e.innerHTML = 'NASCE <b>PREVISTO</b> COM EFEITO NO VENCIMENTO &middot; ENTRA NO "SOBRA AT&Eacute; O FIM DO M&Ecirc;S" SEM MEXER NO SALDO DE HOJE';
    } else {
      e.innerHTML = 'EFEITO IMEDIATO NO SALDO &middot; <b>DATA DO EVENTO = DATA DE EFEITO</b> &middot; SEM CATEGORIA, VAI PARA AS PEND&Ecirc;NCIAS';
    }
  }
  ['#xCartao', '#xParc', '#xData', '#xPara'].forEach((s) => $(s).addEventListener('change', explicar));

  $('#fFull').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = M.parse($('#xValor').value), desc = $('#xDesc').value.trim(), dt = $('#xData').value || S.hoje;
    if (!v) { $('#xErr').textContent = 'VALOR INVÁLIDO'; return; }
    try {
      if (xTipo === 'credito') {
        CB.comprarNoCredito({ cartao: $('#xCartao').value, valor: v, parcelas: Number($('#xParc').value),
          data: dt, descricao: desc, categoria: $('#xCat').value || null });
        toast('COMPRA NO CRÉDITO REGISTRADA');
      } else if (xTipo === 'transf') {
        if ($('#xDe').value === $('#xPara').value) { $('#xErr').textContent = 'ORIGEM E DESTINO IGUAIS'; return; }
        CB.transferir({ de: $('#xDe').value, para: $('#xPara').value, valor: v, data: dt, descricao: desc });
        toast('TRANSFERÊNCIA // PAR DE LANÇAMENTOS CRIADO');
      } else if (xTipo === 'boleto') {
        CB.registrarBoleto({ conta: CB.meio($('#xMeio').value).conta, valor: v, data: dt,
          vencimento: $('#xVenc').value || D.addDias(dt, 10), descricao: desc, categoria: $('#xCat').value || null });
        toast('BOLETO PREVISTO ATÉ O VENCIMENTO');
      } else {
        const m = CB.meio($('#xMeio').value);
        CB.lancar({ conta: m.conta, sentido: xTipo === 'receita' ? 'ENTRADA' : 'SAIDA', valor: v,
          dataEvento: dt, dataEfeito: dt, descricao: desc, categoria: $('#xCat').value || null, meio: m.id, autor: quem });
        toast('REGISTRADO');
      }
      CB.registrar(xTipo + ' ' + M.fmt(v) + ' — ' + desc, 'novo');
      fecharFull(); renderTudo();
    } catch (err) { $('#xErr').textContent = String(err.message || err).toUpperCase(); }
  });

  /* ================= ATALHOS + RENDER ================= */
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') { fecharQA(); fecharFull(); fecharSerie(); $('#ambMenu').classList.remove('open'); }
    const dig = /^(INPUT|SELECT|TEXTAREA)$/.test(document.activeElement.tagName);
    if (!dig && (e.key === 'n' || e.key === 'N')) { e.preventDefault(); abrirQA(); }
  });

  function renderTudo() {
    renderHeader();
    if (tela === 'home') renderHome();
    if (tela === 'extrato') renderExtrato();
    if (tela === 'fatura') renderFatura();
    if (tela === 'series') renderSeries();
    if (tela === 'patrimonio') renderPatrimonio();
    $('#statusLinha').textContent = 'LEDGER SYNCED \u00b7 ' + CB.lancamentos().length + ' LAN\u00c7AMENTOS NO AMBIENTE';
  }

  if (params.get('novo')) toast('IDENTIDADE PROVISIONADA // AMBIENTE CRIADO');
  renderTudo();
})(window.CB);
