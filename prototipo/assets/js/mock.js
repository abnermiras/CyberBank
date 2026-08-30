/* =========================================================================
   CYBERBANK // dados mockados
   Nomes e marcas sao inventados para o clima do genero — nada vem de obra
   existente. O objetivo do seed e deixar TODO o modelo visivel em uma tela:
   previsto e realizado convivendo, uma fatura fechada prestes a vencer,
   outra aberta, um parcelamento atravessando as duas, uma pendencia sem
   categoria, um aporte que nao e gasto e uma aplicacao que rendeu.

   ATENCAO: as categorias do seed (SUSTENTO, MORADIA, LAZER, TRANSITO...) sao do
   USUARIO — o seed representa alguem que ja criou as dele. O sistema NAO cria
   categoria de usuario nenhuma; ele so cria as 14 CATEGORIAS DE SISTEMA, e isso
   acontece em `amb()` (docs/02-dominio/categoria.md).
   ========================================================================= */
(function (CB) {
  'use strict';
  const { S, D } = CB;

  const HOJE = '2026-08-27';
  S.hoje = HOJE;

  // o ambiente nasce com o jogo completo de CATEGORIAS DE SISTEMA — e com nenhuma do usuario
  const amb = (id, nome, cor, setor) => {
    S.ambientes.push({ id, nome, cor, setor });
    CB.criarCategoriasDeSistema(id);
    return id;
  };
  const PESSOAL = amb('amb_pessoal', 'PESSOAL', '#00f0ff', 'NC-77/A');
  const CASA = amb('amb_casa', 'CASA', '#ff1f91', 'NC-77/B');
  S.ambienteAtivo = PESSOAL;

  function novaConta(o) {
    // eixo 3 (docs/02-dominio/conta.md): caixa e CAMPO, e o seed nao tem direito de omiti-lo
    if (typeof o.entraEmCaixa !== 'boolean') throw new Error('conta sem entraEmCaixa: ' + o.nome);
    if (o.entraEmCaixa && !o.entraNoFluxoDeCaixa) throw new Error('caixa implica fluxo: ' + o.nome);
    const c = Object.assign({ id: CB.id('cta'), ultimaAtualizacao: null }, o);
    S.contas.push(c);
    if (o.abertura) {
      S.ambienteAtivo = o.ambiente;
      CB.lancar({ ambiente: o.ambiente, conta: c.id, sentido: 'ENTRADA', valor: o.abertura,
        dataEvento: '2026-07-01', dataEfeito: '2026-07-01', descricao: 'Saldo de abertura',
        situacao: 'REALIZADO', meio: null,
        categoria: CB.catSistema(o.ambiente, 'Saldo de abertura', 'ENTRADA') }).abertura = true;
    }
    return c.id;
  }
  const novoMeio = (o) => { const m = Object.assign({ id: CB.id('mei') }, o); S.meios.push(m); return m.id; };

  function arvore(ambiente, sentido, nome, cor, filhos) {
    const raiz = { id: CB.id('cat'), ambiente, nome, cor, sentido, pai: null, inativa: false };
    S.categorias.push(raiz);
    (filhos || []).forEach((f) => S.categorias.push({ id: CB.id('cat'), ambiente, nome: f, cor,
      sentido, pai: raiz.id, inativa: false }));
    return raiz.id;
  }
  const sub = (raizId, nome) => { const r = S.categorias.find((c) => c.id === raizId);
    return (S.categorias.find((c) => c.pai === raizId && c.nome === nome) || r).id; };

  /* ================= AMBIENTE: PESSOAL ================= */
  const cc = novaConta({ ambiente: PESSOAL, nome: 'HELIX FINANCIAL', apelido: 'Conta corrente',
    tipo: 'CORRENTE', entraNoFluxoDeCaixa: true, entraEmCaixa: true, abertura: 640000 });
  const cash = novaConta({ ambiente: PESSOAL, nome: 'DINHEIRO VIVO', apelido: 'Carteira',
    tipo: 'CARTEIRA', entraNoFluxoDeCaixa: true, entraEmCaixa: true, abertura: 24000 });
  const vr = novaConta({ ambiente: PESSOAL, nome: 'SUSTENANCE CORP', apelido: 'Vale-refeição',
    tipo: 'BENEFICIO', entraNoFluxoDeCaixa: true, entraEmCaixa: false, abertura: 88000 });
  const cold = novaConta({ ambiente: PESSOAL, nome: 'COLD STORAGE', apelido: 'Reserva',
    tipo: 'APLICACAO', entraNoFluxoDeCaixa: false, entraEmCaixa: false, abertura: 1200000 });
  // POUPANCA E UMA APLICACAO, nao um tipo de conta — e existe aqui por um segundo motivo:
  // ela NUNCA foi atualizada desde a abertura (01/07), entao a tela precisa marca-la como
  // DESATUALIZADA. E a regra 7 do CLAUDE.md com banco de provas.
  const pou = novaConta({ ambiente: PESSOAL, nome: 'SLOW VAULT', apelido: 'Poupança',
    tipo: 'APLICACAO', entraNoFluxoDeCaixa: false, entraEmCaixa: false, abertura: 350000 });

  const mDeb = novoMeio({ ambiente: PESSOAL, nome: 'Débito HELIX', tipo: 'DEBITO', conta: cc });
  const mPix = novoMeio({ ambiente: PESSOAL, nome: 'Pix', tipo: 'PIX', conta: cc });
  // ADR-0003: o CONTRATO do cartao e uma CONTA (tipo CARTAO) e o saldo dela e a divida.
  // Limite, ciclo e conta pagadora sao dela, nao do cartao.
  const ccCard = novaConta({ ambiente: PESSOAL, nome: 'OBSIDIAN BLACK', apelido: 'Cartão de crédito',
    tipo: 'CARTAO', entraNoFluxoDeCaixa: true, entraEmCaixa: false, diaVencimento: 28, diasAntesFechamento: 8,
    limite: 1500000, contaPagadora: cc });
  // os cartoes sao MEIOS apontando para ela — fisico e virtual dividem fatura e limite
  const mCard = novoMeio({ ambiente: PESSOAL, nome: 'OBSIDIAN ****-1234', tipo: 'CREDITO', conta: ccCard });
  const mCash = novoMeio({ ambiente: PESSOAL, nome: 'Dinheiro', tipo: 'DINHEIRO', conta: cash });
  const mVr = novoMeio({ ambiente: PESSOAL, nome: 'Cartão benefício', tipo: 'BENEFICIO', conta: vr });
  const mBol = novoMeio({ ambiente: PESSOAL, nome: 'Boleto', tipo: 'BOLETO', conta: cc });

  const cSust = arvore(PESSOAL, 'SAIDA', 'SUSTENTO', '#5cff9d', ['Mercado', 'Restaurante', 'Delivery']);
  const cTran = arvore(PESSOAL, 'SAIDA', 'TRÂNSITO', '#00f0ff', ['Combustível', 'Corrida', 'Estacionamento']);
  const cMora = arvore(PESSOAL, 'SAIDA', 'MORADIA', '#ff6b35', ['Aluguel', 'Energia', 'Rede']);
  const cLaze = arvore(PESSOAL, 'SAIDA', 'LAZER', '#ff1f91', ['Bar', 'Streaming', 'Jogos', 'Academia']);
  const cEqui = arvore(PESSOAL, 'SAIDA', 'EQUIPAMENTO', '#f7f13c', ['Hardware', 'Software']);
  const cRend = arvore(PESSOAL, 'ENTRADA', 'RENDA', '#5cff9d', ['Salário', 'Freela']);
  arvore(PESSOAL, 'ENTRADA', 'OUTROS', '#5f7688', ['Reembolso', 'Presente']);

  S.ambienteAtivo = PESSOAL;
  const L = (o) => CB.lancar(Object.assign({ ambiente: PESSOAL }, o));

  // --- receita e contas do mes ---
  L({ conta: cc, sentido: 'ENTRADA', valor: 850000, dataEvento: '2026-08-05', dataEfeito: '2026-08-05',
    descricao: 'Salário', categoria: sub(cRend, 'Salário'), meio: mPix });
  L({ conta: cc, sentido: 'SAIDA', valor: 220000, dataEvento: '2026-08-10', dataEfeito: '2026-08-10',
    descricao: 'Aluguel', categoria: sub(cMora, 'Aluguel'), meio: mBol });
  L({ conta: cc, sentido: 'SAIDA', valor: 48030, dataEvento: '2026-08-08', dataEfeito: '2026-08-08',
    descricao: 'Mercado NEO-SHIBUYA', categoria: sub(cSust, 'Mercado'), meio: mDeb, estabelecimento: 'NEOSHIBUYA MKT 442' });
  L({ conta: cc, sentido: 'SAIDA', valor: 19900, dataEvento: '2026-08-14', dataEfeito: '2026-08-14',
    descricao: 'Rede neural 1Gb', categoria: sub(cMora, 'Rede'), meio: mDeb });
  L({ conta: cash, sentido: 'SAIDA', valor: 4500, dataEvento: '2026-08-19', dataEfeito: '2026-08-19',
    descricao: 'Corrida noturna', categoria: sub(cTran, 'Corrida'), meio: mCash });
  L({ conta: vr, sentido: 'SAIDA', valor: 3800, dataEvento: '2026-08-24', dataEfeito: '2026-08-24',
    descricao: 'Almoço — RAMEN DECK', categoria: sub(cSust, 'Restaurante'), meio: mVr });
  L({ conta: vr, sentido: 'SAIDA', valor: 4120, dataEvento: '2026-08-26', dataEfeito: '2026-08-26',
    descricao: 'Almoço — SYNTH GRILL', categoria: sub(cSust, 'Restaurante'), meio: mVr });

  // --- boleto ainda nao pago: PREVISTO, dataEfeito = vencimento ---
  CB.registrarBoleto({ conta: cc, valor: 31870, data: '2026-08-22', vencimento: '2026-09-05',
    descricao: 'Energia — setor 7', categoria: sub(cMora, 'Energia') });

  // --- credito ---
  // Helper SO DE SEED: coloca a compra na fatura de uma referencia. No app de verdade
  // o lancamento nasce SEMPRE na fatura ABERTA e so o usuario o move — aqui estamos
  // fabricando passado, o unico caso em que escolher a fatura faz sentido.
  function creditoEm(ref, o) {
    const ls = CB.comprarNoCredito(o);
    let r = ref;
    ls.forEach((l) => {
      l.fatura = CB.faturaDaReferencia(ccCard, r).id;   // so remaneja: a situacao ja
      r = CB.D.proxMes(r);                                // nasceu PROVISIONADO
    });
    return ls;
  }

  // parcelamento atravessando tres faturas: fechada, aberta e futura
  creditoEm('2026-08', { cartao: mCard, valor: 267000, parcelas: 3, data: '2026-07-25',
    descricao: 'Deck neural KIROSHI', categoria: sub(cEqui, 'Hardware'), estabelecimento: 'KIROSHI OPTICS' });
  creditoEm('2026-08', { cartao: mCard, valor: 12850, data: '2026-08-03', descricao: 'Jantar — TOXIC LOUNGE', categoria: sub(cSust, 'Restaurante') });
  creditoEm('2026-08', { cartao: mCard, valor: 3990, data: '2026-08-11', descricao: 'Streaming BRAINDANCE+', categoria: sub(cLaze, 'Streaming') });
  creditoEm('2026-08', { cartao: mCard, valor: 51230, data: '2026-08-18', descricao: 'Mercado NEO-SHIBUYA', categoria: sub(cSust, 'Mercado') });
  creditoEm('2026-09', { cartao: mCard, valor: 9600, data: '2026-08-22', descricao: 'Bar AFTERLIFE DECK', categoria: sub(cLaze, 'Bar') });
  // pendencia de proposito: capturado, sem categoria confirmada
  creditoEm('2026-09', { cartao: mCard, valor: 7420, data: '2026-08-26', descricao: 'MEDTECH 24H', estabelecimento: 'MEDTECH DISP 08' });

  // fatura de agosto fechou dia 20 e vence dia 28 — ainda nao paga.
  // O pagamento previsto e o que faz ela pesar em "sobra ate o fim do mes".
  const fatAgo = S.faturas.find((f) => f.contaCartao === ccCard && f.referencia === '2026-08');
  if (fatAgo) CB.criarPagamentoPrevisto(fatAgo);

  // --- recorrencia: N eventos independentes, sem data de fim (a "Netflix") ---
  // no cartao: a ocorrencia nova nasce quando a fatura fecha e a proxima abre
  CB.criarRecorrencia({ conta: cc, meio: mCard, valor: 3990, dia: 12, inicio: '2026-05-12',
    descricao: 'SYNTH-WAVE PREMIUM', categoria: sub(cLaze, 'Streaming') });
  // fora do cartao: a ocorrencia nova nasce na virada do mes
  CB.criarRecorrencia({ conta: cc, meio: mDeb, valor: 12000, dia: 5, inicio: '2026-06-05',
    descricao: 'ACADEMIA IRONWORKS', categoria: sub(cLaze, 'Academia') });

  // --- aporte: nao e gasto, e o dinheiro trocando de bolso ---
  CB.aportar({ de: cc, para: cold, valor: 50000, data: '2026-08-06', descricao: 'Aporte — reserva' });
  // --- rendimento: informar o valor atual gera a diferenca ---
  CB.atualizarValorAplicacao(cold, 1268400);
  CB.conta(cold).ultimaAtualizacao = '2026-08-25';

  /* ================= AMBIENTE: CASA ================= */
  S.ambienteAtivo = CASA;
  const ccc = novaConta({ ambiente: CASA, nome: 'VOLTA COOP', apelido: 'Conta conjunta',
    tipo: 'CORRENTE', entraNoFluxoDeCaixa: true, entraEmCaixa: true, abertura: 310000 });
  const mDebC = novoMeio({ ambiente: CASA, nome: 'Débito VOLTA', tipo: 'DEBITO', conta: ccc });
  novoMeio({ ambiente: CASA, nome: 'Boleto', tipo: 'BOLETO', conta: ccc });
  const kCasa = arvore(CASA, 'SAIDA', 'CASA', '#ff6b35', ['Mercado', 'Manutenção', 'Contas']);
  const kRenC = arvore(CASA, 'ENTRADA', 'APORTES', '#5cff9d', ['Você', 'Parceiro']);

  const LC = (o) => CB.lancar(Object.assign({ ambiente: CASA }, o));
  LC({ conta: ccc, sentido: 'ENTRADA', valor: 180000, dataEvento: '2026-08-05', dataEfeito: '2026-08-05',
    descricao: 'Rateio do mês', categoria: sub(kRenC, 'Você'), meio: mDebC });
  LC({ conta: ccc, sentido: 'SAIDA', valor: 76400, dataEvento: '2026-08-12', dataEfeito: '2026-08-12',
    descricao: 'Mercado do mês', categoria: sub(kCasa, 'Mercado'), meio: mDebC });
  LC({ conta: ccc, sentido: 'SAIDA', valor: 22000, dataEvento: '2026-08-21', dataEfeito: '2026-08-21',
    descricao: 'Conserto do purificador', categoria: null, meio: mDebC });

  // historia paga: fatura com vencimento no passado ja foi quitada.
  // Pagar e TRANSFERENCIA, entao o ambiente ativo precisa ser o dono do cartao.
  S.ambienteAtivo = PESSOAL;
  // `em(dia, fn)` roda a operacao COM O RELOGIO NAQUELE DIA, para o evento nascer com o
  // `dia` certo. E a unica forma honesta de o seed ter historia em varios dias: no
  // sistema de verdade o relogio andou de verdade, e cada evento foi gravado no dia em
  // que o sistema agiu (docs/02-dominio/evento.md).
  const em = (dia, fn) => { const antes = S.hoje; S.hoje = dia; try { return fn(); } finally { S.hoje = antes; } };
  S.faturas.filter((f) => f.vencimento < HOJE && f.status === 'FECHADA')
    .forEach((f) => { if (CB.totalFatura(f.id) > 0) em(f.vencimento, () => CB.pagarFatura(f.id, { data: f.vencimento })); });

  // faxina de seed: o parcelamento cria FUTURA contando a partir da ABERTA e o
  // helper de historico as remaneja, deixando uma fatura vazia na ponta.
  S.faturas = S.faturas.filter((f) => !(f.status === 'FUTURA' && CB.totalFatura(f.id) === 0));

  // UMA CATEGORIA INATIVA NO SEED, e de proposito com HISTORICO.
  // Existe pelo mesmo motivo da Poupanca nunca atualizada: a regra precisa de algo para
  // marcar. "Academia" tem 3 lancamentos — alguem largou a academia e inativou a
  // categoria. A prova esta em ver os 3 continuarem somando no relatorio de LAZER
  // enquanto a categoria NAO aparece mais no seletor do quick-add.
  // Inativada AQUI, no fim: os lancamentos dela nasceram antes, como na vida real, e o
  // guarda de `lancar` recusaria cria-los depois.
  (function () {
    const aca = S.categorias.find((k) => k.ambiente === PESSOAL && k.nome === 'Academia');
    if (aca) CB.inativarCategoria(aca.id);
  }());

  S.hoje = HOJE;
  // o seed NAO limpa nem inventa evento: o Diario mostra o que o ciclo produziu ao
  // construir a historia, e isso ja foi gravado pelas proprias funcoes do dominio
})(window.CB);
