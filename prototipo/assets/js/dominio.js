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
      serie: o.serie || null,
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
    // Fatura ja paga corrigida: REESCREVE o passado. O valor do pagamento passa a ser o
    // novo total, na data original — sem lancamento de ajuste. Nada a recalcular: saldo e
    // sempre a soma dos lancamentos, entao reescrever o valor ja refaz tudo que deriva dele.
    const antes = f.totalNoFechamento;
    f.totalNoFechamento = total;
    f.status = f.jaFoiPaga ? 'PAGA' : 'FECHADA';
    if (f.jaFoiPaga && antes !== null && antes !== total) {
      registrar(`fatura ${f.referencia}: pagamento reescrito ${M.fmt(antes)} -> ${M.fmt(total)}`, 'fatura');
    } else {
      registrar(`fatura ${f.referencia} fechada em ${M.fmt(total)}`, 'fatura');
    }
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
    let grupo = null;
    if (n > 1) {
      grupo = id('ser');
      S.series.push({ id: grupo, ambiente: S.ambienteAtivo, tipo: 'PARCELAMENTO', descricao: o.descricao,
        valorTotal: o.valor, parcelas: n, categoria: o.categoria || null, cartao: o.cartao, ativa: true });
    }
    for (let i = 0; i < n; i++) {
      const dataEv = i === 0 ? (o.data || S.hoje) : D.addMeses(o.data || S.hoje, i);
      const f = faturaPara(o.cartao, dataEv);
      const l = lancar({
        conta: c.conta, sentido: 'SAIDA', valor: base + (i === 0 ? resto : 0),
        dataEvento: dataEv, dataEfeito: f.vencimento,
        descricao: n > 1 ? `${o.descricao} ${i + 1}/${n}` : o.descricao,
        situacao: 'PREVISTO', categoria: o.categoria || null, meio: o.cartao,
        fatura: f.id, serie: grupo, origemParcelamento: grupo, estabelecimento: o.estabelecimento || null
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

  /* ================= SERIES: RECORRENCIA E PARCELAMENTO =================
     Recorrencia: N eventos independentes, sem fim. Pode perguntar ao editar.
     Parcelamento: UMA compra dividida. Nunca pergunta — altera todas.
     Regras em docs/02-dominio/recorrencia.md
     ==================================================================== */
  /* Recorrencia NAO tem horizonte. Um previsto nao e so item de tela: ele segura
     limite do cartao e sugere que a assinatura acaba. Assinatura nao acaba e nao
     reserva limite de mes que nao chegou.

     A geracao segue o CICLO, uma ocorrencia por vez:
       - recorrencia no cartao  -> quando a fatura fecha, a proxima abre e ganha a sua
       - recorrencia fora dele  -> na virada do mes
     Resultado: no maximo UMA ocorrencia nao-acontecida por recorrencia. */

  const lancamentosDaSerie = (sid) => S.lancamentos.filter((l) => l.serie === sid);
  const serie = (sid) => S.series.find((x) => x.id === sid);
  const noCartao = (r) => !!(r.meio && meio(r.meio) && meio(r.meio).tipo === 'CREDITO');

  // o ciclo que esta aberto agora: a fatura aberta do cartao, ou o mes corrente
  function cicloAberto(r) {
    if (!noCartao(r)) return D.mes(S.hoje);
    return faturaPara(r.meio, S.hoje).referencia;  // a fatura que uma compra de hoje pegaria
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

  /* Garante que existe uma ocorrencia para cada ciclo desde o inicio ATE o ciclo
     aberto — e nenhuma alem dele. Idempotente: rodar de novo nao duplica. */
  function sincronizarRecorrencia(sid) {
    const r = serie(sid);
    if (!r || r.tipo !== 'RECORRENCIA' || !r.ativa) return [];
    const alvo = cicloAberto(r);
    const jaTem = {};
    lancamentosDaSerie(sid).forEach((l) => {
      const f = l.fatura ? S.faturas.find((x) => x.id === l.fatura) : null;
      jaTem[f ? f.referencia : D.mes(l.dataEvento)] = true;
    });

    const criados = [];
    let cursor = r.inicio.slice(0, 8) + String(Math.min(r.dia, 28)).padStart(2, '0');
    let guarda = 0;
    while (guarda++ < 400) {
      const ciclo = noCartao(r) ? faturaPara(r.meio, cursor).referencia : D.mes(cursor);
      if (ciclo > alvo) break;                       // nunca passa do ciclo aberto
      if (!jaTem[ciclo]) {
        if (noCartao(r)) {
          const l = comprarNoCredito({ cartao: r.meio, valor: r.valor, data: cursor,
            descricao: r.descricao, categoria: r.categoria })[0];
          l.serie = sid;
          criados.push(l);
        } else {
          const l = lancar({ ambiente: r.ambiente, conta: r.conta, sentido: 'SAIDA', valor: r.valor,
            dataEvento: cursor, dataEfeito: cursor, descricao: r.descricao,
            situacao: cursor <= S.hoje ? 'REALIZADO' : 'PREVISTO',
            categoria: r.categoria, meio: r.meio, serie: sid });
          criados.push(l);
        }
        jaTem[ciclo] = true;
      }
      cursor = D.addMeses(cursor, 1);
    }
    return criados;
  }

  const sincronizarRecorrencias = (filtro) => S.series
    .filter((r) => r.tipo === 'RECORRENCIA' && r.ativa && (!filtro || filtro(r)))
    .forEach((r) => sincronizarRecorrencia(r.id));

  /* ---------- limite do cartao ----------
     Disponivel = limite - tudo que foi comprado e ainda nao foi pago. Parcela futura
     SEGURA limite (5.000 em 10x come 5.000 e libera 500 por mes), e por isso mesmo
     recorrencia nao pode gerar previsto: seguraria limite de mes que nao chegou. */
  function limiteDisponivel(cartaoId) {
    const c = meio(cartaoId);
    if (!c || !c.limite) return null;
    const preso = S.lancamentos.filter((l) => {
      if (l.meio !== cartaoId || !l.fatura) return false;
      const f = S.faturas.find((x) => x.id === l.fatura);
      return f && f.status !== 'PAGA';
    }).reduce((s, l) => s + l.valor, 0);
    return { limite: c.limite, preso, disponivel: c.limite - preso };
  }

  /* ---------- quais faturas precisam ser reabertas para esta edicao ----------
     O usuario ve isso ANTES de confirmar: reabrir fatura paga de dois meses atras
     nao pode ser efeito colateral silencioso. */
  function faturasAfetadas(lancs) {
    const ids = {};
    lancs.forEach((l) => { if (l.fatura) { const f = S.faturas.find((x) => x.id === l.fatura);
      if (f && f.status !== 'ABERTA') ids[f.id] = f; } });
    return Object.values(ids);
  }

  // aplica uma edicao reabrindo e refechando o que for preciso
  function aplicarComReabertura(lancs, campos, quem) {
    const reabrir = faturasAfetadas(lancs);
    reabrir.forEach((f) => reabrirFatura(f.id));
    lancs.forEach((l) => editar(l.id, campos, quem));
    reabrir.forEach((f) => fecharFatura(f.id));
    return reabrir;
  }

  /* ---------- PARCELAMENTO: altera TODAS. Nunca pergunta. ---------- */
  function editarParcelamento(sid, novoValorTotal, quem) {
    const r = serie(sid);
    if (!r || r.tipo !== 'PARCELAMENTO') throw new Error('série não é parcelamento');
    const ls = lancamentosDaSerie(sid).sort((a, b) => a.dataEvento.localeCompare(b.dataEvento));
    const n = ls.length;
    const base = Math.floor(novoValorTotal / n), resto = novoValorTotal - base * n;
    const reabertas = faturasAfetadas(ls);
    reabertas.forEach((f) => reabrirFatura(f.id));
    ls.forEach((l, i) => editar(l.id, { valor: base + (i === 0 ? resto : 0) }, quem));
    reabertas.forEach((f) => fecharFatura(f.id));
    r.valorTotal = novoValorTotal;
    registrar(`parcelamento "${r.descricao}" alterado para ${M.fmt(novoValorTotal)} em ${n}x`, 'edit');
    return { alterados: n, faturasReabertas: reabertas.length };
  }

  /* ---------- RECORRENCIA: pergunta. escopo = 'FUTURAS' | 'TODAS' ---------- */
  function editarRecorrencia(sid, campos, escopo, quem) {
    const r = serie(sid);
    if (!r || r.tipo !== 'RECORRENCIA') throw new Error('série não é recorrência');
    if (campos.valor) r.valor = campos.valor;
    if (campos.categoria !== undefined) r.categoria = campos.categoria;

    const todos = lancamentosDaSerie(sid);
    const alvo = escopo === 'TODAS' ? todos : todos.filter((l) => l.dataEvento > S.hoje);
    const reabertas = aplicarComReabertura(alvo, campos, quem);
    registrar(`recorrência "${r.descricao}": ${escopo === 'TODAS' ? 'passado também' : 'só as futuras'} (${alvo.length})`, 'edit');
    return { alterados: alvo.length, faturasReabertas: reabertas.length };
  }

  // previa do impacto, para a tela perguntar com numero na mao
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
    const reabertas = faturasAfetadas(previstos);
    reabertas.forEach((f) => reabrirFatura(f.id));
    const ids = {}; previstos.forEach((l) => { ids[l.id] = true; });
    S.lancamentos = S.lancamentos.filter((l) => !ids[l.id]);
    reabertas.forEach((f) => fecharFatura(f.id));
    r.ativa = false; r.canceladaEm = S.hoje;
    registrar(`"${r.descricao}" cancelada — ${previstos.length} previsto(s) removido(s)`, 'edit');
    return { removidos: previstos.length };
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
      // GATILHO 1 — virada do mes: recorrencia fora do cartao ganha a ocorrencia do mes
      if (D.diaDoMes(S.hoje) === 1) sincronizarRecorrencias((r) => !noCartao(r));
      // fatura fecha sozinha no dia do fechamento
      S.faturas.forEach((f) => {
        if (f.status === 'ABERTA' && f.fechamento <= S.hoje && totalFatura(f.id) > 0) {
          fecharFatura(f.id);
          eventos.push(`${D.br(S.hoje)} — fatura ${f.referencia} fechou: ${M.fmt(totalFatura(f.id))}`);
          // GATILHO 2 — a fatura fechou, a proxima abriu: as recorrencias ativas entram nela
          const antes = S.lancamentos.length;
          sincronizarRecorrencias((r) => noCartao(r) && r.meio === f.cartao);
          if (S.lancamentos.length > antes) {
            eventos.push(`${D.br(S.hoje)} — ${S.lancamentos.length - antes} recorrência(s) lançada(s) na fatura nova`);
          }
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
    faturaAberta, comprarNoCredito, registrarBoleto, avancar,
    series, serie, lancamentosDaSerie, criarRecorrencia,
    sincronizarRecorrencia, sincronizarRecorrencias, cicloAberto, noCartao, limiteDisponivel,
    editarParcelamento, editarRecorrencia, previaEdicaoRecorrencia, cancelarSerie,
    faturasAfetadas
  };
})(window);
