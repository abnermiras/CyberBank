/* =========================================================================
   CYBERBANK // motor de dominio do prototipo
   Implementa as decisoes de docs/02-dominio/. Nao e codigo de producao:
   existe para VALIDAR as regras antes de codar. Se algo aqui ficou dificil
   de escrever, e sinal de que a regra precisa voltar para a mesa.

   Decisoes exercitadas aqui:
   - valor sempre positivo, o sinal vem de `sentido`
   - duas datas: dataEvento (relatorio) e dataEfeito (saldo)
   - situacao PREVISTO|REALIZADO; REALIZADO nunca volta atras
   - saldo = soma dos lancamentos, nunca armazenado
   - transferencia = par de lancamentos com o mesmo transferenciaId
   - ADR-0003: o contrato de cartao E UMA CONTA (tipo CARTAO). A compra debita
     ela; pagar a fatura e TRANSFERENCIA; o que nao foi pago fica como saldo
   - fatura = recorte de periodo da conta CARTAO. FUTURA | ABERTA | FECHADA
   - o lancamento entra na fatura pelo STATUS, nunca pela data
   - nada congela: lancamento de fatura fechada se edita direto
   ========================================================================= */
(function (global) {
  'use strict';

  /* ---------- datas (ISO 'YYYY-MM-DD', sem fuso, sem drama) ---------- */
  const D = {
    hoje: () => new Date().toISOString().slice(0, 10),
    parse: (s) => { const [a, m, d] = s.split('-').map(Number); return new Date(a, m - 1, d); },
    fmt: (dt) => `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`,
    addDias: (s, n) => { const dt = D.parse(s); dt.setDate(dt.getDate() + n); return D.fmt(dt); },
    addMeses: (s, n) => { const dt = D.parse(s); const dia = dt.getDate(); dt.setDate(1); dt.setMonth(dt.getMonth() + n);
      dt.setDate(Math.min(dia, new Date(dt.getFullYear(), dt.getMonth() + 1, 0).getDate())); return D.fmt(dt); },
    mes: (s) => s.slice(0, 7),
    proxMes: (ref) => D.mes(D.addMeses(ref + '-01', 1)),
    diaDoMes: (s) => Number(s.slice(8, 10)),
    fimDoMes: (s) => { const dt = D.parse(s + ''); return D.fmt(new Date(dt.getFullYear(), dt.getMonth() + 1, 0)); },
    noMes: (s, mes) => s.slice(0, 7) === mes,
    br: (s) => s.split('-').reverse().join('/'),
    diasEntre: (a, b) => Math.round((D.parse(b) - D.parse(a)) / 86400000)
  };
  const SEMPRE = '9999-12-31';

  /* ---------- dinheiro: SEMPRE inteiro em centavos ---------- */
  const M = {
    fmt: (c) => (c < 0 ? '-' : '') + 'R$ ' + (Math.abs(c) / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
    curto: (c) => { const v = Math.abs(c) / 100; const s = c < 0 ? '-' : '';
      if (v >= 1000) return s + 'R$ ' + (v / 1000).toFixed(1).replace('.', ',') + 'k';
      return s + 'R$ ' + v.toFixed(0); },
    parse: (txt) => Math.round(parseFloat(String(txt).replace(/\./g, '').replace(',', '.')) * 100) || 0
  };

  let seq = 1;
  const id = (p) => `${p}_${seq++}`;

  /* ================= ESTADO ================= */
  const S = {
    hoje: null,
    ambienteAtivo: null,
    ambientes: [], contas: [], meios: [], categorias: [],
    lancamentos: [], faturas: [], series: [],
    log: []
  };

  const registrar = (txt, tipo) => { S.log.unshift({ t: S.hoje, txt, tipo: tipo || 'sys' }); if (S.log.length > 60) S.log.pop(); };

  /* ---------- escopo: TUDO e filtrado por ambiente, sem excecao ---------- */
  const doAmbiente = (col) => col.filter((x) => x.ambiente === S.ambienteAtivo);
  const contas = () => doAmbiente(S.contas);
  const meios = () => doAmbiente(S.meios);
  const categorias = () => doAmbiente(S.categorias);
  const lancamentos = () => doAmbiente(S.lancamentos);
  const faturas = () => doAmbiente(S.faturas);
  const series = () => doAmbiente(S.series).filter((x) => x.ativa);

  const conta = (cid) => S.contas.find((c) => c.id === cid);
  const meio = (mid) => S.meios.find((m) => m.id === mid);
  const categoria = (kid) => S.categorias.find((k) => k.id === kid);
  const raizDe = (kid) => { const k = categoria(kid); return k ? (k.pai ? categoria(k.pai) : k) : null; };
  const ehDivida = (c) => !!c && c.tipo === 'CARTAO';
  // contas de CAIXA: as de fluxo de caixa que nao sao divida.
  // ACHADO: `entraNoFluxoDeCaixa` responde "movimento e gasto?", nao "isso e caixa?".
  // A conta CARTAO e a primeira em que as duas perguntas divergem.
  const ehCaixa = (c) => !!c && c.entraNoFluxoDeCaixa && !ehDivida(c);

  /* ================= LANCAMENTO ================= */
  function lancar(o) {
    if (!o.valor || o.valor <= 0) throw new Error('valor tem que ser positivo — o sinal vem do sentido');
    const l = {
      id: id('lan'),
      ambiente: o.ambiente || S.ambienteAtivo,   // o ambiente de QUEM LANCOU, nunca o da conta
      conta: o.conta,
      sentido: o.sentido,                    // ENTRADA | SAIDA
      valor: o.valor,                        // centavos, sempre > 0
      dataEvento: o.dataEvento || S.hoje,
      dataEfeito: o.dataEfeito || o.dataEvento || S.hoje,
      descricao: o.descricao || 'Sem descrição',
      situacao: o.situacao || 'REALIZADO',   // PREVISTO | REALIZADO
      categoria: o.categoria || null,        // null = pendência (que é consulta, não estado)
      meio: o.meio || null,
      fatura: o.fatura || null,
      transferenciaId: o.transferenciaId || null,
      pagamentoDeFatura: o.pagamentoDeFatura || null,
      serie: o.serie || null,
      origemParcelamento: o.origemParcelamento || null,
      estabelecimento: o.estabelecimento || null,
      autor: o.autor || 'V',
      historico: []
    };
    S.lancamentos.push(l);
    // lancamento novo numa fatura ja fechada muda o total dela: o pagamento
    // previsto acompanha enquanto nao foi feito
    if (l.fatura) sincronizarPagamentoPrevisto(l.fatura);
    return l;
  }

  // Edicao direta com historico. NADA CONGELA: fatura fechada nao trava lancamento.
  // O sistema nao tem a palavra final sobre o dinheiro do usuario — ele mostra a
  // consequencia antes e guarda quem mudou o que.
  function editar(lid, campos, quem) {
    const l = S.lancamentos.find((x) => x.id === lid);
    if (!l) return null;
    Object.keys(campos).forEach((c) => {
      if (l[c] === campos[c]) return;
      l.historico.push({ quando: S.hoje, quem: quem || 'V', campo: c, de: l[c], para: campos[c] });
      l[c] = campos[c];
    });
    if (l.fatura) sincronizarPagamentoPrevisto(l.fatura);
    registrar(`lançamento editado: ${l.descricao}`, 'edit');
    return l;
  }

  // estorno != correcao: o dinheiro voltou de verdade. Fato novo, na fatura ABERTA.
  function estornar(lid) {
    const l = S.lancamentos.find((x) => x.id === lid);
    if (!l) return null;
    const alvo = l.fatura ? (faturaAberta(conta(l.conta).id) || null) : null;
    const e = lancar({
      conta: l.conta, sentido: l.sentido === 'SAIDA' ? 'ENTRADA' : 'SAIDA',
      valor: l.valor, descricao: 'Estorno — ' + l.descricao,
      categoria: l.categoria, meio: l.meio, situacao: 'REALIZADO',
      fatura: alvo ? alvo.id : null
    });
    e.estornoDe = l.id; l.estornadoPor = e.id;
    registrar(`estorno de ${l.descricao}`, 'estorno');
    return e;
  }

  /* ================= TRANSFERENCIA (par ligado) ================= */
  function transferir(o) {
    const tid = id('tr');
    const base = { transferenciaId: tid, dataEvento: o.data || S.hoje, dataEfeito: o.data || S.hoje,
      situacao: o.situacao || 'REALIZADO', categoria: null, meio: null,
      pagamentoDeFatura: o.pagamentoDeFatura || null };
    const saida = lancar(Object.assign({}, base, { conta: o.de, sentido: 'SAIDA', valor: o.valor, descricao: o.descricao }));
    const entrada = lancar(Object.assign({}, base, { conta: o.para, sentido: 'ENTRADA', valor: o.valor, descricao: o.descricao }));
    return [saida, entrada];
  }

  const aportar = (o) => transferir({ de: o.de, para: o.para, valor: o.valor, data: o.data, descricao: o.descricao || 'Aporte' });
  const resgatar = (o) => transferir({ de: o.de, para: o.para, valor: o.valor, data: o.data, descricao: o.descricao || 'Resgate' });
  const parDaTransferencia = (tid) => S.lancamentos.filter((l) => l.transferenciaId === tid);

  /* rendimento: informar o valor atual gera a DIFERENCA como lancamento */
  function atualizarValorAplicacao(cid, valorInformado) {
    const c = conta(cid);
    const atual = saldoRealizado(cid, S.hoje);
    const dif = valorInformado - atual;
    c.ultimaAtualizacao = S.hoje;
    if (dif === 0) { registrar(`${c.nome}: sem variação`, 'rend'); return null; }
    const l = lancar({ conta: cid, sentido: dif > 0 ? 'ENTRADA' : 'SAIDA', valor: Math.abs(dif),
      descricao: dif > 0 ? 'Rendimento' : 'Desvalorização', situacao: 'REALIZADO', categoria: null, meio: null });
    l.rendimento = true;
    registrar(`${c.nome}: ${dif > 0 ? 'rendeu' : 'perdeu'} ${M.fmt(Math.abs(dif))}`, 'rend');
    return l;
  }

  /* ================= SALDO ================= */
  const sinal = (l) => (l.sentido === 'ENTRADA' ? 1 : -1);

  function saldoRealizado(cid, ate) {
    const lim = ate || S.hoje;
    return S.lancamentos.filter((l) => l.conta === cid && l.situacao === 'REALIZADO' && l.dataEfeito <= lim)
      .reduce((s, l) => s + sinal(l) * l.valor, 0);
  }
  function saldoProjetado(cid, ate) {
    const lim = ate || D.fimDoMes(S.hoje);
    return S.lancamentos.filter((l) => l.conta === cid && l.dataEfeito <= lim)
      .reduce((s, l) => s + sinal(l) * l.valor, 0);
  }
  const somaContas = (filtro, fn) => contas().filter(filtro).reduce((s, c) => s + fn(c.id), 0);

  const emCaixa = () => somaContas(ehCaixa, (cid) => saldoRealizado(cid));
  const sobraAteFimDoMes = () => somaContas(ehCaixa, (cid) => saldoProjetado(cid, D.fimDoMes(S.hoje)));
  const guardado = () => somaContas((c) => !c.entraNoFluxoDeCaixa, (cid) => saldoRealizado(cid));

  // PATRIMONIO: saldo REALIZADO de todas as contas, e o PROJETADO das de divida.
  // "Divida futura ja e sua; receita futura ainda nao." A parcela de 2027 pesa hoje;
  // o salario do mes que vem, nao.
  const patrimonio = () => somaContas(() => true,
    (cid) => (ehDivida(conta(cid)) ? -dividaCartao(cid) : saldoRealizado(cid)));

  /* DIVIDA — achado do prototipo (28/08).
     O saldo projetado da conta CARTAO NAO serve como divida: ele abate o pagamento
     PREVISTO, que ainda nao aconteceu, e responde outra pergunta ("quanto vou dever
     depois de pagar"). Divida e o que foi comprado e ainda nao foi pago, entao ela
     soma tudo MENOS os pagamentos previstos. Limite e patrimonio usam esta. */
  const dividaCartao = (cid) => -S.lancamentos
    .filter((l) => l.conta === cid && !(l.pagamentoDeFatura && l.situacao === 'PREVISTO'))
    .reduce((s, l) => s + sinal(l) * l.valor, 0);
  const dividaTotal = () => contas().filter(ehDivida).reduce((s, c) => s + dividaCartao(c.id), 0);

  /* ================= RELATORIOS ================= */
  // gasto por categoria usa dataEvento e SO contas de fluxo de caixa — a CARTAO
  // inclusive, porque comprar no cartao E gasto da vida. Pagar a fatura nao conta
  // de novo: pagamento e transferencia, e transferencia nunca entra aqui.
  function gastoPorCategoria(mes) {
    const alvo = mes || D.mes(S.hoje);
    const mapa = {};
    lancamentos().forEach((l) => {
      if (l.transferenciaId || l.rendimento) return;
      if (l.sentido !== 'SAIDA') return;
      if (!D.noMes(l.dataEvento, alvo)) return;
      const c = conta(l.conta); if (!c || !c.entraNoFluxoDeCaixa) return;
      const r = l.categoria ? raizDe(l.categoria) : null;
      const chave = r ? r.id : '__sem__';
      if (!mapa[chave]) mapa[chave] = { id: chave, nome: r ? r.nome : 'Sem categoria', cor: r ? r.cor : '#5f7688', total: 0, itens: 0 };
      mapa[chave].total += l.valor; mapa[chave].itens++;
    });
    return Object.values(mapa).sort((a, b) => b.total - a.total);
  }

  function guardadoNoMes(mes) {
    const alvo = mes || D.mes(S.hoje);
    return lancamentos().filter((l) => l.transferenciaId && l.sentido === 'ENTRADA' && D.noMes(l.dataEvento, alvo))
      .filter((l) => { const c = conta(l.conta); return c && !c.entraNoFluxoDeCaixa; })
      .reduce((s, l) => s + l.valor, 0);
  }

  function receitaDoMes(mes) {
    const alvo = mes || D.mes(S.hoje);
    return lancamentos().filter((l) => !l.transferenciaId && !l.rendimento && l.sentido === 'ENTRADA' && D.noMes(l.dataEvento, alvo))
      .filter((l) => { const c = conta(l.conta); return ehCaixa(conta(l.conta)); })
      .reduce((s, l) => s + l.valor, 0);
  }

  // PENDENCIA: lancamento sem categoria QUE ESPERA UMA.
  const esperaCategoria = (l) => !l.transferenciaId && !l.rendimento && !l.abertura;
  const pendencias = () => lancamentos().filter((l) => !l.categoria && esperaCategoria(l));

  const extrato = (filtro) => lancamentos()
    .filter((l) => (filtro && filtro.conta ? l.conta === filtro.conta : true))
    .slice().sort((a, b) => (b.dataEvento === a.dataEvento ? b.id.localeCompare(a.id) : b.dataEvento.localeCompare(a.dataEvento)));

  /* ================= FATURA =================
     A fatura e o recorte de um periodo da conta CARTAO. Tem estado proprio
     (FUTURA | ABERTA | FECHADA) porque estado nao se deriva; o VALOR dela e
     sempre soma dos lancamentos, nunca armazenado.
     ========================================================================= */

  // ciclo: o usuario informa dia do vencimento + quantos dias antes fecha
  function datasDaRef(cc, ref) {
    const c = conta(cc);
    const ano = Number(ref.slice(0, 4)), m = Number(ref.slice(5, 7));
    const diaV = Math.min(c.diaVencimento, new Date(ano, m, 0).getDate());
    const vencimento = D.fmt(new Date(ano, m - 1, diaV));
    return { vencimento, fechamento: D.addDias(vencimento, -c.diasAntesFechamento) };
  }

  const faturasDe = (cc) => S.faturas.filter((f) => f.contaCartao === cc)
    .sort((a, b) => a.referencia.localeCompare(b.referencia));

  function novaFatura(cc, ref, status) {
    const c = conta(cc);
    const dt = datasDaRef(cc, ref);
    const f = { id: id('fat'), ambiente: c.ambiente, contaCartao: cc, referencia: ref,
      fechamento: dt.fechamento, vencimento: dt.vencimento, status: status || 'FUTURA' };
    S.faturas.push(f);
    return f;
  }

  // a referencia cujo ciclo ainda esta correndo hoje
  function referenciaCorrente(cc) {
    let ref = D.mes(S.hoje);
    for (let i = 0; i < 36; i++) {
      if (datasDaRef(cc, ref).fechamento >= S.hoje) return ref;
      ref = D.proxMes(ref);
    }
    return ref;
  }

  // SO UMA fatura ABERTA por conta CARTAO. Se nao existe, nasce agora.
  function faturaAberta(cc) {
    const ja = faturasDe(cc).find((f) => f.status === 'ABERTA');
    if (ja) return ja;
    const ref = referenciaCorrente(cc);
    const existente = faturasDe(cc).find((f) => f.referencia === ref);
    if (existente) { existente.status = 'ABERTA'; return existente; }
    return novaFatura(cc, ref, 'ABERTA');
  }

  // posicao 0 = a ABERTA; i > 0 = i ciclos a frente, criada como FUTURA se preciso.
  // E assim que o parcelamento acha as faturas que ainda nao existem.
  function faturaNaPosicao(cc, i) {
    const aberta = faturaAberta(cc);
    if (i === 0) return aberta;
    let ref = aberta.referencia;
    for (let k = 0; k < i; k++) ref = D.proxMes(ref);
    return faturasDe(cc).find((f) => f.referencia === ref) || novaFatura(cc, ref, 'FUTURA');
  }

  const totalFatura = (fid) => S.lancamentos
    .filter((l) => l.fatura === fid).reduce((s, l) => s + sinal(l) * -1 * l.valor, 0);
  const lancamentosDaFatura = (fid) => S.lancamentos.filter((l) => l.fatura === fid);

  /* ---------- pagamento: EIXO DERIVADO, nao estado salvo ---------- */
  const pagamentosDaFatura = (fid) => S.lancamentos
    .filter((l) => l.pagamentoDeFatura === fid && l.sentido === 'ENTRADA');
  const pagoDaFatura = (fid) => pagamentosDaFatura(fid)
    .filter((l) => l.situacao === 'REALIZADO').reduce((s, l) => s + l.valor, 0);

  function situacaoPagamento(fid) {
    const total = totalFatura(fid), pago = pagoDaFatura(fid);
    if (pago <= 0) return 'EM ABERTO';
    return pago >= total ? 'QUITADA' : 'PARCIAL';
  }

  // Enquanto o pagamento previsto nao foi feito, ele acompanha o total da fatura.
  function sincronizarPagamentoPrevisto(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!f || f.status !== 'FECHADA') return null;
    const total = totalFatura(fid);
    const pendentes = pagamentosDaFatura(fid).filter((l) => l.situacao === 'PREVISTO');
    const jaPago = pagoDaFatura(fid);
    const falta = total - jaPago;
    if (!pendentes.length) return null;
    const par = parDaTransferencia(pendentes[0].transferenciaId);
    if (falta <= 0) { S.lancamentos = S.lancamentos.filter((l) => par.indexOf(l) < 0); return null; }
    par.forEach((l) => { l.valor = falta; });
    return par;
  }

  function criarPagamentoPrevisto(f) {
    const c = conta(f.contaCartao);
    const total = totalFatura(f.id) - pagoDaFatura(f.id);
    if (total <= 0 || !c.contaPagadora) return null;
    if (pagamentosDaFatura(f.id).some((l) => l.situacao === 'PREVISTO')) return null;
    const par = transferir({ de: c.contaPagadora, para: f.contaCartao, valor: total,
      data: f.vencimento, descricao: 'Pagamento fatura ' + f.referencia,
      situacao: 'PREVISTO', pagamentoDeFatura: f.id });
    return par[0];
  }

  /* ---------- fechar: o gatilho de mais coisa do que parece ---------- */
  function fecharFatura(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!f || f.status !== 'ABERTA') return f;
    f.status = 'FECHADA';
    // 1. as parcelas previstas daquele ciclo viram realizadas: a divida consolidou
    lancamentosDaFatura(fid).forEach((l) => {
      if (l.situacao === 'PREVISTO') { l.situacao = 'REALIZADO'; l.dataEfeito = f.fechamento; }
    });
    // 2. a seguinte abre (criada se nao existir)
    const seguinteRef = D.proxMes(f.referencia);
    const seg = faturasDe(f.contaCartao).find((x) => x.referencia === seguinteRef) ||
      novaFatura(f.contaCartao, seguinteRef, 'FUTURA');
    seg.status = 'ABERTA';
    // 3. as recorrencias ativas do cartao entram na fatura recem-aberta (idempotente)
    sincronizarRecorrencias((r) => noCartao(r) && meio(r.meio) && meio(r.meio).conta === f.contaCartao);
    // 4. nasce o pagamento previsto — e ele que mantem "quanto sobra ate o fim do mes"
    criarPagamentoPrevisto(f);
    registrar(`fatura ${f.referencia} fechada em ${M.fmt(totalFatura(fid))}`, 'fatura');
    return f;
  }

  /* ---------- abrir: SO a ultima fechada. Contingencia, nao fluxo ----------
     Serve para uma coisa: o ciclo ainda esta correndo e o sistema achou que tinha
     acabado (a operadora fechou em outro dia). A seguinte volta a FUTURA. */
  const ultimaFechada = (cc) => faturasDe(cc).filter((f) => f.status === 'FECHADA').slice(-1)[0] || null;
  const podeAbrir = (f) => !!f && f.status === 'FECHADA' && ultimaFechada(f.contaCartao) &&
    ultimaFechada(f.contaCartao).id === f.id;

  function abrirFatura(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!podeAbrir(f)) return null;
    const aberta = faturasDe(f.contaCartao).find((x) => x.status === 'ABERTA');
    if (aberta) aberta.status = 'FUTURA';   // o que ja estava nela FICA onde esta
    // o pagamento previsto ainda nao pago some; renasce no proximo fechamento
    const prev = pagamentosDaFatura(fid).filter((l) => l.situacao === 'PREVISTO');
    prev.forEach((l) => { const par = parDaTransferencia(l.transferenciaId);
      S.lancamentos = S.lancamentos.filter((x) => par.indexOf(x) < 0); });
    f.status = 'ABERTA';
    registrar(`fatura ${f.referencia} ABERTA — contingência`, 'fatura');
    return f;
  }

  /* ---------- pagar: TRANSFERENCIA. Nao ha duplo computo e nao ha regra dizendo
     que nao ha: transferencia nao tem categoria e nao entra em gasto. O gasto foi
     contado uma vez, na compra. ---------- */
  function pagarFatura(fid, o) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!f || f.status === 'ABERTA') return null;
    o = o || {};
    const c = conta(f.contaCartao);
    const falta = totalFatura(fid) - pagoDaFatura(fid);
    const valor = o.valor || falta;
    const de = o.conta || c.contaPagadora;
    const data = o.data || S.hoje;
    if (valor <= 0) return null;

    const previsto = pagamentosDaFatura(fid).find((l) => l.situacao === 'PREVISTO');
    if (previsto) {
      const par = parDaTransferencia(previsto.transferenciaId);
      par.forEach((l) => { l.valor = valor; l.situacao = 'REALIZADO'; l.dataEvento = data; l.dataEfeito = data; });
      const saida = par.find((l) => l.sentido === 'SAIDA');
      if (saida.conta !== de) saida.conta = de;
    } else {
      transferir({ de, para: f.contaCartao, valor, data,
        descricao: 'Pagamento fatura ' + f.referencia, pagamentoDeFatura: fid });
    }
    // o que nao foi pago SIMPLESMENTE FICA como saldo da conta CARTAO.
    // Nao existe "saldo da fatura anterior" como lancamento: nada rola.
    const sit = situacaoPagamento(fid);
    if (sit === 'PARCIAL') criarPagamentoPrevisto(f);
    registrar(`fatura ${f.referencia}: pago ${M.fmt(valor)} de ${conta(de).apelido || conta(de).nome} — ${sit}`, 'fatura');
    return f;
  }

  /* ---------- correcao em fatura paga: o sistema PERGUNTA ----------
     Duas respostas legitimas: o banco cobrou o valor novo (ajusta o pagamento), ou
     o pagamento foi o que foi (a diferenca vira saldo). */
  function previaCorrecaoFatura(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    const total = totalFatura(fid), pago = pagoDaFatura(fid);
    return { fatura: f, total, pago, diferenca: total - pago, situacao: situacaoPagamento(fid) };
  }
  function ajustarPagamento(fid) {
    const p = previaCorrecaoFatura(fid);
    const real = pagamentosDaFatura(fid).filter((l) => l.situacao === 'REALIZADO');
    if (!real.length || p.diferenca === 0) return null;
    const par = parDaTransferencia(real[real.length - 1].transferenciaId);
    const novo = par[0].valor + p.diferenca;
    if (novo <= 0) return null;
    par.forEach((l) => { l.valor = novo; });
    registrar(`fatura ${p.fatura.referencia}: pagamento ajustado para ${M.fmt(novo)}`, 'fatura');
    return novo;
  }

  /* ---------- compra no credito: debita a conta CARTAO ----------
     A fatura vem do STATUS, nunca da data: a compra entra na ABERTA, e as parcelas
     seguintes nas FUTURA. Quando o palpite erra, quem move e o usuario. */
  function comprarNoCredito(o) {
    const c = meio(o.cartao);
    const cc = c.conta;                       // a conta CARTAO do contrato
    const n = o.parcelas || 1;
    const base = Math.floor(o.valor / n);
    const resto = o.valor - base * n;
    const criados = [];
    let grupo = null;
    if (n > 1) {
      grupo = id('ser');
      S.series.push({ id: grupo, ambiente: S.ambienteAtivo, tipo: 'PARCELAMENTO', descricao: o.descricao,
        valorTotal: o.valor, parcelas: n, categoria: o.categoria || null, cartao: o.cartao, ativa: true });
    }
    for (let i = 0; i < n; i++) {
      const f = faturaNaPosicao(cc, i);
      const dataEv = i === 0 ? (o.data || S.hoje) : D.addMeses(o.data || S.hoje, i);
      // comprou, deve: a parcela do ciclo corrente ja e REALIZADO.
      // As de mes que nao chegou sao PREVISTO, com efeito no fechamento da sua fatura.
      const previsto = i > 0;
      const l = lancar({
        conta: cc, sentido: 'SAIDA', valor: base + (i === 0 ? resto : 0),
        dataEvento: dataEv, dataEfeito: previsto ? f.fechamento : (o.data || S.hoje),
        descricao: n > 1 ? `${o.descricao} ${i + 1}/${n}` : o.descricao,
        situacao: previsto ? 'PREVISTO' : 'REALIZADO',
        categoria: o.categoria || null, meio: o.cartao,
        fatura: f.id, serie: grupo, origemParcelamento: grupo, estabelecimento: o.estabelecimento || null
      });
      criados.push(l);
    }
    return criados;
  }

  /* ---------- boleto: previsto no vencimento, realizado quando pago ---------- */
  function registrarBoleto(o) {
    const b = meios().find((m) => m.tipo === 'BOLETO');
    return lancar({ conta: o.conta, sentido: 'SAIDA', valor: o.valor,
      dataEvento: o.data || S.hoje, dataEfeito: o.vencimento,
      descricao: o.descricao, situacao: 'PREVISTO', categoria: o.categoria || null,
      meio: b ? b.id : null });
  }

  /* ================= SERIES: RECORRENCIA E PARCELAMENTO =================
     Recorrencia: N eventos independentes, sem fim. Pode perguntar ao editar.
     Parcelamento: UMA compra dividida. Nunca pergunta — altera todas.
     ==================================================================== */
  const lancamentosDaSerie = (sid) => S.lancamentos.filter((l) => l.serie === sid);
  const serie = (sid) => S.series.find((x) => x.id === sid);
  const noCartao = (r) => !!(r.meio && meio(r.meio) && meio(r.meio).tipo === 'CREDITO');
  const contaDoCartao = (r) => meio(r.meio).conta;

  function faturaDaReferencia(cc, ref) {
    const ja = faturasDe(cc).find((f) => f.referencia === ref);
    if (ja) return ja;
    const dt = datasDaRef(cc, ref);
    const status = dt.fechamento < S.hoje ? 'FECHADA'
      : (faturasDe(cc).some((f) => f.status === 'ABERTA') ? 'FUTURA' : 'ABERTA');
    return novaFatura(cc, ref, status);
  }

  function cicloAberto(r) {
    if (!noCartao(r)) return D.mes(S.hoje);
    return faturaAberta(contaDoCartao(r)).referencia;
  }

  function criarRecorrencia(o) {
    const r = {
      id: id('ser'), ambiente: o.ambiente || S.ambienteAtivo, tipo: 'RECORRENCIA',
      descricao: o.descricao, valor: o.valor, dia: o.dia || D.diaDoMes(S.hoje),
      conta: o.conta, meio: o.meio || null, categoria: o.categoria || null,
      inicio: o.inicio || S.hoje, ativa: true,
      automatico: !!o.automatico   // "debito automatico" e atributo daqui, nao meio
    };
    S.series.push(r);
    sincronizarRecorrencia(r.id);
    registrar(`recorrência criada: ${r.descricao} — ${M.fmt(r.valor)}/mês`, 'novo');
    return r;
  }

  /* Uma ocorrencia por ciclo, e NENHUMA alem do ciclo aberto. Idempotente:
     o fechamento chama isto e nunca lanca a mesma assinatura duas vezes. */
  function sincronizarRecorrencia(sid) {
    const r = serie(sid);
    if (!r || r.tipo !== 'RECORRENCIA' || !r.ativa) return [];
    const criados = [];
    const jaTem = {};
    lancamentosDaSerie(sid).forEach((l) => {
      const f = l.fatura ? S.faturas.find((x) => x.id === l.fatura) : null;
      jaTem[f ? f.referencia : D.mes(l.dataEvento)] = true;
    });
    const alvo = cicloAberto(r);
    let ref = D.mes(r.inicio);
    let guarda = 0;
    while (ref <= alvo && guarda++ < 400) {
      if (!jaTem[ref]) {
        const dia = String(Math.min(r.dia, 28)).padStart(2, '0');
        if (noCartao(r)) {
          const cc = contaDoCartao(r);
          const f = faturaDaReferencia(cc, ref);
          const fechada = f.status === 'FECHADA';
          const l = lancar({ ambiente: r.ambiente, conta: cc, sentido: 'SAIDA', valor: r.valor,
            dataEvento: ref + '-' + dia, dataEfeito: ref + '-' + dia,
            descricao: r.descricao, situacao: 'REALIZADO',
            categoria: r.categoria, meio: r.meio, fatura: f.id, serie: sid });
          criados.push(l);
        } else {
          const quando = ref + '-' + dia;
          const l = lancar({ ambiente: r.ambiente, conta: r.conta, sentido: 'SAIDA', valor: r.valor,
            dataEvento: quando, dataEfeito: quando, descricao: r.descricao,
            situacao: quando <= S.hoje ? 'REALIZADO' : 'PREVISTO',
            categoria: r.categoria, meio: r.meio, serie: sid });
          criados.push(l);
        }
        jaTem[ref] = true;
      }
      ref = D.proxMes(ref);
    }
    return criados;
  }

  const sincronizarRecorrencias = (filtro) => S.series
    .filter((r) => r.tipo === 'RECORRENCIA' && r.ativa && (!filtro || filtro(r)))
    .forEach((r) => sincronizarRecorrencia(r.id));

  /* ---------- limite: do CONTRATO, ou seja, da conta CARTAO ----------
     Disponivel = limite - saldo PROJETADO. O projetado ja inclui a parcela futura,
     e por isso parcela segura limite — e por isso recorrencia nao gera previsto. */
  function limiteDisponivel(x) {
    let c = conta(x);
    if (!c) { const m = meio(x); c = m ? conta(m.conta) : null; }
    if (!c || !ehDivida(c) || !c.limite) return null;
    const preso = dividaCartao(c.id);   // ver o achado em DIVIDA, acima
    return { conta: c.id, limite: c.limite, preso, disponivel: c.limite - preso };
  }

  /* ---------- o que muda de valor nesta edicao ----------
     Nao ha mais reabertura: mostrar QUAIS faturas mudam de valor continua sendo
     obrigatorio, porque mexer numa fatura paga nao pode ser silencioso. */
  function faturasAfetadas(lancs) {
    const ids = {};
    lancs.forEach((l) => { if (l.fatura) { const f = S.faturas.find((x) => x.id === l.fatura);
      if (f && f.status !== 'ABERTA') ids[f.id] = f; } });
    return Object.values(ids);
  }

  function aplicarEdicao(lancs, campos, quem) {
    const tocadas = faturasAfetadas(lancs);
    lancs.forEach((l) => editar(l.id, campos, quem));
    return tocadas;
  }

  /* ---------- PARCELAMENTO: altera TODAS. Nunca pergunta. ---------- */
  function editarParcelamento(sid, novoValorTotal, quem) {
    const r = serie(sid);
    if (!r || r.tipo !== 'PARCELAMENTO') throw new Error('série não é parcelamento');
    const ls = lancamentosDaSerie(sid).sort((a, b) => a.dataEvento.localeCompare(b.dataEvento));
    const n = ls.length;
    const base = Math.floor(novoValorTotal / n), resto = novoValorTotal - base * n;
    const tocadas = faturasAfetadas(ls);
    ls.forEach((l, i) => editar(l.id, { valor: base + (i === 0 ? resto : 0) }, quem));
    r.valorTotal = novoValorTotal;
    registrar(`parcelamento "${r.descricao}" alterado para ${M.fmt(novoValorTotal)} em ${n}x`, 'edit');
    return { alterados: n, faturasTocadas: tocadas.length };
  }

  /* ---------- RECORRENCIA: pergunta. escopo = 'FUTURAS' | 'TODAS' ---------- */
  function editarRecorrencia(sid, campos, escopo, quem) {
    const r = serie(sid);
    if (!r || r.tipo !== 'RECORRENCIA') throw new Error('série não é recorrência');
    if (campos.valor) r.valor = campos.valor;
    if (campos.categoria !== undefined) r.categoria = campos.categoria;
    const todos = lancamentosDaSerie(sid);
    const alvo = escopo === 'TODAS' ? todos : todos.filter((l) => l.dataEvento > S.hoje);
    const tocadas = aplicarEdicao(alvo, campos, quem);
    registrar(`recorrência "${r.descricao}": ${escopo === 'TODAS' ? 'passado também' : 'só as futuras'} (${alvo.length})`, 'edit');
    return { alterados: alvo.length, faturasTocadas: tocadas.length };
  }

  function previaEdicaoRecorrencia(sid, escopo) {
    const todos = lancamentosDaSerie(sid);
    const alvo = escopo === 'TODAS' ? todos : todos.filter((l) => l.dataEvento > S.hoje);
    return { ocorrencias: alvo.length, faturas: faturasAfetadas(alvo) };
  }

  /* ---------- CANCELAR: os previstos a frente somem; o passado fica ---------- */
  function cancelarSerie(sid) {
    const r = serie(sid);
    if (!r) return null;
    const previstos = lancamentosDaSerie(sid).filter((l) => l.situacao === 'PREVISTO');
    const tocadas = faturasAfetadas(previstos);
    const ids = {}; previstos.forEach((l) => { ids[l.id] = true; });
    S.lancamentos = S.lancamentos.filter((l) => !ids[l.id]);
    tocadas.forEach((f) => sincronizarPagamentoPrevisto(f.id));
    r.ativa = false; r.canceladaEm = S.hoje;
    registrar(`"${r.descricao}" cancelada — ${previstos.length} previsto(s) removido(s)`, 'edit');
    return { removidos: previstos.length };
  }

  /* ================= RELOGIO — o ciclo simulavel ================= */
  function avancar(dias) {
    const eventos = [];
    for (let i = 0; i < dias; i++) {
      S.hoje = D.addDias(S.hoje, 1);
      // previsto vira realizado quando a dataEfeito chega.
      // Fora: o que esta preso em fatura (vira no fechamento) e o pagamento de
      // fatura (quem paga e o usuario — o sistema nao debita sozinho).
      S.lancamentos.forEach((l) => {
        if (l.situacao === 'PREVISTO' && l.dataEfeito <= S.hoje && !l.fatura && !l.pagamentoDeFatura) {
          l.situacao = 'REALIZADO';
          eventos.push(`${D.br(S.hoje)} — ${l.descricao} caiu: ${M.fmt(l.valor)}`);
          registrar(`${l.descricao} realizado`, 'auto');
        }
      });
      // GATILHO 1 — virada do mes: recorrencia fora do cartao ganha a ocorrencia
      if (D.diaDoMes(S.hoje) === 1) sincronizarRecorrencias((r) => !noCartao(r));
      // GATILHO 2 — a fatura fecha sozinha; abre a seguinte, lanca recorrencias
      // e cria o pagamento previsto. Idempotente e recupera atraso.
      S.faturas.slice().forEach((f) => {
        if (f.status === 'ABERTA' && f.fechamento <= S.hoje) {
          const total = totalFatura(f.id);
          const antes = S.lancamentos.length;
          fecharFatura(f.id);
          eventos.push(`${D.br(S.hoje)} — fatura ${f.referencia} fechou: ${M.fmt(total)}`);
          const novos = S.lancamentos.length - antes - 2;  // -2 = o par do pagamento previsto
          if (novos > 0) eventos.push(`${D.br(S.hoje)} — ${novos} recorrência(s) lançada(s) na fatura nova`);
          if (total > 0) eventos.push(`${D.br(S.hoje)} — pagamento previsto de ${M.fmt(total)} em ${D.br(f.vencimento)}`);
        }
      });
    }
    return eventos;
  }

  global.CB = {
    S, D, M, id, registrar, SEMPRE,
    contas, meios, categorias, lancamentos, faturas,
    conta, meio, categoria, raizDe, ehDivida, ehCaixa,
    lancar, editar, estornar, transferir, aportar, resgatar, atualizarValorAplicacao,
    saldoRealizado, saldoProjetado, emCaixa, sobraAteFimDoMes, patrimonio, guardado,
    dividaCartao, dividaTotal,
    gastoPorCategoria, guardadoNoMes, receitaDoMes, pendencias, esperaCategoria, extrato,
    faturasDe, faturaAberta, faturaNaPosicao, faturaDaReferencia, datasDaRef,
    totalFatura, lancamentosDaFatura, pagamentosDaFatura, pagoDaFatura, situacaoPagamento,
    fecharFatura, abrirFatura, podeAbrir, ultimaFechada, pagarFatura,
    previaCorrecaoFatura, ajustarPagamento, criarPagamentoPrevisto, sincronizarPagamentoPrevisto,
    comprarNoCredito, registrarBoleto, avancar,
    series, serie, lancamentosDaSerie, criarRecorrencia,
    sincronizarRecorrencia, sincronizarRecorrencias, cicloAberto, noCartao, limiteDisponivel,
    editarParcelamento, editarRecorrencia, previaEdicaoRecorrencia, cancelarSerie,
    faturasAfetadas, aplicarEdicao
  };
})(window);
