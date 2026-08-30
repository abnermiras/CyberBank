#!/usr/bin/env node
/* =========================================================================
   CYBERBANK // verificar.js — o banco de provas em UM comando, sem navegador.
       node prototipo/verificar.js

   Existe por causa de um bug que ficou dois dias vivo: a decisao de 29/08
   apagou `esperaCategoria` do dominio e ninguem apagou as duas chamadas da
   tela. O Extrato e a Home morriam no primeiro render, e `conferir()` nao
   podia acusar — ele confere o MODELO, e o que quebrou foi a TELA contra a
   API do modelo. Sao duas perguntas diferentes, e agora as duas tem resposta
   automatica.

   Sai com codigo 1 se qualquer prova falhar. Rodar antes de todo commit que
   mexa em prototipo/.
   ========================================================================= */
'use strict';
const fs = require('fs');
const path = require('path');
const JS = path.join(__dirname, 'assets', 'js');

global.window = global;
require(path.join(JS, 'dominio.js'));
require(path.join(JS, 'mock.js'));
const CB = global.CB, M = CB.M;
const S_HOJE = CB.S.hoje;

let falhas = 0;
const prova = (titulo, ok, detalhe) => {
  if (!ok) falhas++;
  console.log((ok ? '  ok   ' : '  FALHA') + '  ' + titulo + (detalhe ? '  ->  ' + detalhe : ''));
};
const secao = (t) => console.log('\n' + t);

/* ---- 1. a TELA contra a API do MODELO -----------------------------------
   Todo `CB.alguma_coisa` que app.js e mock.js chamam tem que existir no
   objeto que dominio.js exporta. Estatico, burro e teria pego o bug. */
secao('1. a tela chama so o que o dominio exporta');
const usados = new Map();
['app.js', 'mock.js'].forEach((f) => {
  // comentario fora: o proprio comentario que documenta este bug cita
  // `CB.esperaCategoria`, e um scanner ingenuo se acusaria sozinho
  const txt = fs.readFileSync(path.join(JS, f), 'utf8')
    .replace(/\/\*[\s\S]*?\*\//g, ' ').replace(/^\s*\/\/.*$/gm, ' ');
  const re = /\bCB\.([A-Za-z_$][\w$]*)/g;
  let m;
  while ((m = re.exec(txt))) if (!usados.has(m[1])) usados.set(m[1], f);
});
const orfas = [...usados].filter(([nome]) => CB[nome] === undefined);
prova(usados.size + ' simbolos CB.* usados, todos existem', orfas.length === 0,
  orfas.length ? orfas.map(([n, f]) => 'CB.' + n + ' (' + f + ')').join(', ') : '');

/* ---- 2. toda tela renderiza sem estourar --------------------------------
   Nao substitui o navegador, mas pega o erro de simbolo e o de dado: e a
   diferenca entre "a tela esta feia" e "a tela nao existe". */
secao('2. as consultas que cada tela usa respondem');
const telas = {
  home: () => [CB.emCaixa(), CB.patrimonio(), CB.dividaTotal(), CB.pendencias(), CB.gastoPorCategoria()],
  extrato: () => CB.extrato(null).map((l) => [CB.conta(l.conta), l.categoria && CB.raizDe(l.categoria)]),
  fatura: () => CB.faturas().map((f) => [CB.totalFatura(f.id), CB.lancamentosDaFatura(f.id)]),
  series: () => CB.series().map((s) => CB.lancamentosDaSerie(s.id)),
  reserva: () => CB.contas().map((c) => [CB.saldoRealizado(c.id), CB.saldoProjetado(c.id, S_HOJE)]),
  cadastro: () => CB.categorias().map((k) => CB.catEscolhivel(k)),
};
Object.keys(telas).forEach((nome) => {
  let erro = null;
  try { telas[nome](); } catch (e) { erro = e.message; }
  prova(nome, !erro, erro || '');
});

/* ---- 3. as invariantes dos docs ----------------------------------------- */
secao('3. conferir() — toda invariante escrita, sobre o estado inteiro');
const viol = CB.conferir();
prova('sem violacao', viol.length === 0, viol.join(' | '));

/* ---- 4. os numeros do seed ---------------------------------------------- */
secao('4. os numeros do seed (PESSOAL, 27/08/2026)');
const cartao = CB.contas().find((c) => c.tipo === 'CARTAO');
[['em caixa', CB.emCaixa(), 1123600],
 ['divida', CB.dividaTotal(), 356080],
 ['patrimonio', CB.patrimonio(), 2466000],
 ['limite disponivel', CB.limiteDisponivel(cartao.id), 1143920],
].forEach(([nome, tem, esperado]) => {
  const v = (tem && typeof tem === 'object') ? (tem.disponivel !== undefined ? tem.disponivel : tem.valor) : tem;
  prova(nome, v === esperado, M.fmt(v) + (v === esperado ? '' : ' (esperado ' + M.fmt(esperado) + ')'));
});

/* ---- 5. as duas conferencias que valem para sempre ---------------------- */
secao('5. as duas conferencias que valem para sempre');
const semCat = CB.lancamentos().filter((l) =>
  (l.transferenciaId || l.rendimento || l.abertura || l.rolagemDeFatura) && !l.categoria);
prova('nenhum lancamento do ciclo sem categoria', semCat.length === 0,
  semCat.map((l) => l.descricao).join(', '));
prova('a fila de pendencias do seed tem 1 item', CB.pendencias().length === 1,
  CB.pendencias().map((l) => l.descricao).join(', '));

/* ---- 6. a inativacao de categoria --------------------------------------- */
secao('6. inativacao (docs/02-dominio/categoria.md, secao Inativar)');
const cat = (n) => CB.categorias().find((k) => k.nome === n);
const escolhiveis = (s) => CB.categoriasEscolhiveis(s).map((k) => k.nome);
const recusa = (fn) => { try { fn(); return null; } catch (e) { return e.message; } };

prova('seed: Academia inativa esta fora da escolha', !escolhiveis('SAIDA').includes('Academia'));
prova('seed: e mantem os 3 lancamentos dela', CB.lancamentosDaCategoria(cat('Academia').id).length === 3);
prova('lancamento do usuario em inativa e recusado',
  /inativa/.test(recusa(() => CB.lancar({ conta: CB.contas()[0].id, sentido: 'SAIDA', valor: 100,
    dataEvento: '2026-08-27', dataEfeito: '2026-08-27', descricao: 't', categoria: cat('Academia').id })) || ''));
prova('o CICLO continua lancando na inativa',
  recusa(() => CB.lancar({ conta: CB.contas()[0].id, sentido: 'SAIDA', valor: 100,
    dataEvento: '2026-08-27', dataEfeito: '2026-08-27', descricao: 't', categoria: cat('Academia').id,
    doCiclo: true })) === null);

['Bar', 'Streaming', 'Jogos'].forEach((n) => CB.inativarCategoria(cat(n).id));
prova('(b) sem filho ativo, a raiz volta a ser escolhivel', escolhiveis('SAIDA').includes('LAZER'));
CB.reativarCategoria(cat('Bar').id);
prova('(b) reativar um filho tira a raiz de novo', !escolhiveis('SAIDA').includes('LAZER'));

CB.inativarCategoria(cat('SUSTENTO').id);
prova('(a) raiz inativa tira os filhos da escolha', !escolhiveis('SAIDA').includes('Mercado'));
prova('(a) e nao muda o campo deles',
  CB.filhosDe(cat('SUSTENTO').id).every((f) => f.inativa === false));
CB.inativarCategoria(cat('Delivery').id);
CB.reativarCategoria(cat('SUSTENTO').id);
const volta = escolhiveis('SAIDA');
prova('(a) reativar devolve exatamente o que estava ativo',
  volta.includes('Mercado') && volta.includes('Restaurante') && !volta.includes('Delivery'));

prova('excluir com historico e recusado',
  /nao se exclui/.test(recusa(() => CB.excluirCategoria(cat('SUSTENTO').id)) || ''));
const nova = CB.criarCategoria({ nome: '__T__', sentido: 'SAIDA' });
CB.criarCategoria({ nome: '__F__', pai: nova.id });
prova('excluir sem historico leva a arvore inteira', CB.excluirCategoria(nova.id) === 2);
prova('categoria de sistema nao se inativa',
  /sistema/.test(recusa(() => CB.inativarCategoria(CB.categorias().find((k) => k.sistema).id)) || ''));
prova('nenhuma subcategoria trocou de raiz (mover nao existe)',
  CB.categorias().every((k) => !k.pai || CB.categoria(k.pai)));

secao('conferir() depois de mexer em tudo');
const viol2 = CB.conferir();
prova('ainda sem violacao', viol2.length === 0, viol2.join(' | '));

console.log('\n' + (falhas ? '>>> ' + falhas + ' PROVA(S) FALHARAM' : '>>> TUDO PASSOU'));
process.exit(falhas ? 1 : 0);
