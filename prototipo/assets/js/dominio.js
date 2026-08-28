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
   - aporte/resgate sao transferencia; nao tem categoria; nao sao gasto
   - rendimento e lancamento de diferenca, nao sobrescrita de saldo
   - fatura fechada congela; reabrir -> editar -> recalcular -> ajuste -> fechar
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
    diaDoMes: (s) => Number(s.slice(8, 10)),
    fimDoMes: (s) => { const dt = D.parse(s + ''); return D.fmt(new Date(dt.getFullYear(), dt.getMonth() + 1, 0)); },
    noMes: (s, mes) => s.slice(0, 7) === mes,
    ate: (s, limite) => s <= limite,
    br: (s) => s.split('-').reverse().join('/'),
    diasEntre: (a, b) => Math.round((D.parse(b) - D.parse(a)) / 86400000)
  };

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
    lancamentos: [], faturas: [],
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

  const conta = (cid) => S.contas.find((c) => c.id === cid);
  const meio = (mid) => S.meios.find((m) => m.id === mid);
  const categoria = (kid) => S.categorias.find((k) => k.id === kid);
  const raizDe = (kid) => { const k = categoria(kid); return k ? (k.pai ? categoria(k.pai) : k) : null; };

  /* ================= LANCAMENTO ================= */
  function lancar(o) {
    if (!o.valor || o.valor <= 0) throw new Error('valor tem que ser positivo — o sinal vem do sentido');
    const l = {
      id: id('lan'),
      ambiente: o.ambiente || S.ambienteAtivo,
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
      origemParcelamento: o.origemParcelamento || null,
      estabelecimento: o.estabelecimento || null,
      autor: o.autor || 'V',
      historico: []
    };
    S.lancamentos.push(l);
    return l;
  }

  // edicao direta com historico — a decisao de 27/08
  function editar(lid, campos, quem) {
    const l = S.lancamentos.find((x) => x.id === lid);
    if (!l) return null;
    if (l.fatura) {
      const f = S.faturas.find((x) => x.id === l.fatura);
      if (f && f.status !== 'ABERTA') throw new Error('fatura fechada congela o lançamento — reabra a fatura antes');
    }
    Object.keys(campos).forEach((c) => {
      if (l[c] === campos[c]) return;
      l.historico.push({ quando: S.hoje, quem: quem || 'V', campo: c, de: l[c], para: campos[c] });
      l[c] = campos[c];
    });
    registrar(`lançamento editado: ${l.descricao}`, 'edit');
    return l;
  }

  // estorno != correcao: o dinheiro voltou de verdade
  function estornar(lid) {
    const l = S.lancamentos.find((x) => x.id === lid);
    if (!l) return null;
    const e = lancar({
      conta: l.conta, sentido: l.sentido === 'SAIDA' ? 'ENTRADA' : 'SAIDA',
      valor: l.valor, descricao: 'Estorno — ' + l.descricao,
      categoria: l.categoria, meio: l.meio, situacao: 'REALIZADO'
    });
    e.estornoDe = l.id; l.estornadoPor = e.id;
    registrar(`estorno de ${l.descricao}`, 'estorno');
    return e;
  }

  /* ================= TRANSFERENCIA (par ligado) ================= */
  function transferir(o) {
    const tid = id('tr');
    const base = { transferenciaId: tid, dataEvento: o.data || S.hoje, dataEfeito: o.data || S.hoje,
      situacao: 'REALIZADO', categoria: null, meio: null };
    const saida = lancar(Object.assign({}, base, { conta: o.de, sentido: 'SAIDA', valor: o.valor, descricao: o.descricao }));
    const entrada = lancar(Object.assign({}, base, { conta: o.para, sentido: 'ENTRADA', valor: o.valor, descricao: o.descricao }));
    return [saida, entrada];
  }

  const aportar = (o) => transferir({ de: o.de, para: o.para, valor: o.valor, data: o.data, descricao: o.descricao || 'Aporte' });
  const resgatar = (o) => transferir({ de: o.de, para: o.para, valor: o.valor, data: o.data, descricao: o.descricao || 'Resgate' });

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

  const emCaixa = () => somaContas((c) => c.entraNoFluxoDeCaixa, (cid) => saldoRealizado(cid));
  const sobraAteFimDoMes = () => somaContas((c) => c.entraNoFluxoDeCaixa, (cid) => saldoProjetado(cid, D.fimDoMes(S.hoje)));
  const patrimonio = () => somaContas(() => true, (cid) => saldoRealizado(cid));
  const guardado = () => somaContas((c) => !c.entraNoFluxoDeCaixa, (cid) => saldoRealizado(cid));

  /* ================= RELATORIOS ================= */
  // gasto por categoria usa dataEvento e SO contas de fluxo de caixa.
  // transferencia, aporte, resgate e rendimento nao tem categoria => ficam fora.
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

  // o que foi GUARDADO no mes: aportes para contas fora do fluxo de caixa.
  // e a linha que impede o usuario de procurar o dinheiro que "sumiu".
  function guardadoNoMes(mes) {
    const alvo = mes || D.mes(S.hoje);
    return lancamentos().filter((l) => l.transferenciaId && l.sentido === 'ENTRADA' && D.noMes(l.dataEvento, alvo))
      .filter((l) => { const c = conta(l.conta); return c && !c.entraNoFluxoDeCaixa; })
      .reduce((s, l) => s + l.valor, 0);
  }

  function receitaDoMes(mes) {
    const alvo = mes || D.mes(S.hoje);
    return lancamentos().filter((l) => !l.transferenciaId && !l.rendimento && l.sentido === 'ENTRADA' && D.noMes(l.dataEvento, alvo))
      .filter((l) => { const c = conta(l.conta); return c && c.entraNoFluxoDeCaixa; })
      .reduce((s, l) => s + l.valor, 0);
  }

  // PENDENCIA: lancamento sem categoria QUE ESPERA UMA. Transferencia, rendimento,
  // ajuste e abertura de conta nao tem categoria por natureza — nao sao pendencia.
  // (Achado do prototipo: a definicao "lancamento sem categoria" pegava a abertura.)
  const esperaCategoria = (l) => !l.transferenciaId && !l.rendimento && !l.abertura && !l.ajusteDeFatura;
  const pendencias = () => lancamentos().filter((l) => !l.categoria && esperaCategoria(l));

  const extrato = (filtro) => lancamentos()
    .filter((l) => (filtro && filtro.conta ? l.conta === filtro.conta : true))
    .slice().sort((a, b) => (b.dataEvento === a.dataEvento ? b.id.localeCompare(a.id) : b.dataEvento.localeCompare(a.dataEvento)));

  /* ================= FATURA ================= */
  // A compra cai na fatura do ciclo corrente se o dia <= fechamento; senao, na seguinte.
  // dataEfeito do lancamento de credito = vencimento da fatura em que caiu.
  function faturaPara(cartaoId, dataEvento) {
    const c = meio(cartaoId);
    const diaEv = D.diaDoMes(dataEvento);
    let refBase = dataEvento.slice(0, 7);
    if (diaEv > c.diaFechamento) refBase = D.mes(D.addMeses(dataEvento + '', 1));
    let f = S.faturas.find((x) => x.cartao === cartaoId && x.referencia === refBase);
    if (!f) {
      const ano = Number(refBase.slice(0, 4)), m = Number(refBase.slice(5, 7));
      const dtFech = D.fmt(new Date(ano, m - 1, Math.min(c.diaFechamento, new Date(ano, m, 0).getDate())));
      const venc = D.fmt(new Date(ano, m - 1, c.diaVencimento));
      f = { id: id('fat'), ambiente: c.ambiente, cartao: cartaoId, referencia: refBase,
        fechamento: dtFech, vencimento: venc > dtFech ? venc : D.addMeses(venc, 1),
        status: 'ABERTA', pagoEm: null, totalNoFechamento: null };
      S.faturas.push(f);
    }
    return f;
  }

  const totalFatura = (fid) => S.lancamentos.filter((l) => l.fatura === fid).reduce((s, l) => s + sinal(l) * -1 * l.valor, 0);
  const lancamentosDaFatura = (fid) => S.lancamentos.filter((l) => l.fatura === fid);

  function fecharFatura(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!f || f.status !== 'ABERTA') return f;
    const total = totalFatura(fid);
    // reabertura de fatura ja paga: a diferenca vira AJUSTE, e nao um REALIZADO virando PREVISTO
    if (f.jaFoiPaga && f.totalNoFechamento !== null && total !== f.totalNoFechamento) {
      const dif = total - f.totalNoFechamento;
      const aj = lancar({ conta: meio(f.cartao).conta, sentido: dif > 0 ? 'SAIDA' : 'ENTRADA', valor: Math.abs(dif),
        descricao: `Ajuste da fatura ${f.referencia}`, dataEvento: S.hoje, dataEfeito: S.hoje,
        situacao: 'REALIZADO', categoria: null, meio: null });
      aj.ajusteDeFatura = f.id;
      registrar(`ajuste de ${M.fmt(Math.abs(dif))} na fatura ${f.referencia}`, 'fatura');
    }
    f.totalNoFechamento = total;
    f.status = f.jaFoiPaga ? 'PAGA' : 'FECHADA';
    registrar(`fatura ${f.referencia} fechada em ${M.fmt(total)}`, 'fatura');
    return f;
  }

  function reabrirFatura(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!f || f.status === 'ABERTA') return f;
    f.status = 'ABERTA';
    registrar(`fatura ${f.referencia} REABERTA`, 'fatura');
    return f;
  }

  // pagar NAO cria lancamento novo: os proprios lancamentos da fatura debitam a conta.
  // Criar um lancamento de pagamento contaria o gasto duas vezes.
  function pagarFatura(fid) {
    const f = S.faturas.find((x) => x.id === fid);
    if (!f || f.status !== 'FECHADA') return f;
    lancamentosDaFatura(fid).forEach((l) => { l.situacao = 'REALIZADO'; l.dataEfeito = S.hoje; });
    f.status = 'PAGA'; f.pagoEm = S.hoje; f.jaFoiPaga = true;
    registrar(`fatura ${f.referencia} paga — ${M.fmt(totalFatura(fid))}`, 'fatura');
    return f;
  }

  const faturaAberta = (cartaoId) => S.faturas.filter((f) => f.cartao === cartaoId && f.status === 'ABERTA')
    .sort((a, b) => a.referencia.localeCompare(b.referencia))[0] || null;

  /* ---------- compra no credito, com parcelamento ---------- */
  function comprarNoCredito(o) {
    const c = meio(o.cartao);
    const n = o.parcelas || 1;
    const base = Math.floor(o.valor / n);
    const resto = o.valor - base * n;
    const criados = [];
    const grupo = n > 1 ? id('parc') : null;
    for (let i = 0; i < n; i++) {
      const dataEv = i === 0 ? (o.data || S.hoje) : D.addMeses(o.data || S.hoje, i);
      const f = faturaPara(o.cartao, dataEv);
      const l = lancar({
        conta: c.conta, sentido: 'SAIDA', valor: base + (i === 0 ? resto : 0),
        dataEvento: dataEv, dataEfeito: f.vencimento,
        descricao: n > 1 ? `${o.descricao} ${i + 1}/${n}` : o.descricao,
        situacao: 'PREVISTO', categoria: o.categoria || null, meio: o.cartao,
        fatura: f.id, origemParcelamento: grupo, estabelecimento: o.estabelecimento || null
      });
      criados.push(l);
    }
    return criados;
  }

  /* ---------- boleto: previsto no vencimento, realizado quando pago ---------- */
  function registrarBoleto(o) {
    return lancar({ conta: o.conta, sentido: 'SAIDA', valor: o.valor,
      dataEvento: o.data || S.hoje, dataEfeito: o.vencimento,
      descricao: o.descricao, situacao: 'PREVISTO', categoria: o.categoria || null,
      meio: meios().find((m) => m.tipo === 'BOLETO') ? meios().find((m) => m.tipo === 'BOLETO').id : null });
  }

  /* ================= RELOGIO — o ciclo simulavel ================= */
  function avancar(dias) {
    const eventos = [];
    for (let i = 0; i < dias; i++) {
      S.hoje = D.addDias(S.hoje, 1);
      // previsto vira realizado quando a data de efeito chega (menos o que esta preso em fatura)
      S.lancamentos.forEach((l) => {
        if (l.situacao === 'PREVISTO' && l.dataEfeito <= S.hoje && !l.fatura) {
          l.situacao = 'REALIZADO';
          eventos.push(`${D.br(S.hoje)} — ${l.descricao} caiu: ${M.fmt(l.valor)}`);
          registrar(`${l.descricao} realizado`, 'auto');
        }
      });
      // fatura fecha sozinha no dia do fechamento
      S.faturas.forEach((f) => {
        if (f.status === 'ABERTA' && f.fechamento <= S.hoje && totalFatura(f.id) > 0) {
          fecharFatura(f.id);
          eventos.push(`${D.br(S.hoje)} — fatura ${f.referencia} fechou: ${M.fmt(totalFatura(f.id))}`);
        }
      });
    }
    return eventos;
  }

  global.CB = {
    S, D, M, id, registrar,
    contas, meios, categorias, lancamentos, faturas,
    conta, meio, categoria, raizDe,
    lancar, editar, estornar, transferir, aportar, resgatar, atualizarValorAplicacao,
    saldoRealizado, saldoProjetado, emCaixa, sobraAteFimDoMes, patrimonio, guardado,
    gastoPorCategoria, guardadoNoMes, receitaDoMes, pendencias, esperaCategoria, extrato,
    faturaPara, totalFatura, lancamentosDaFatura, fecharFatura, reabrirFatura, pagarFatura,
    faturaAberta, comprarNoCredito, registrarBoleto, avancar
  };
})(window);
