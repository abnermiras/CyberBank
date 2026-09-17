const Contexto = { ambiente: null };

const TELAS = [
  { id: 'home', rotulo: 'HOME', icone: '◈' },
  { id: 'extrato', rotulo: 'EXTRATO', icone: '≡' },
  { id: 'fatura', rotulo: 'FATURA', icone: '▤' },
  { id: 'series', rotulo: 'SÉRIES', icone: '∞' },
  { id: 'reserva', rotulo: 'RESERVA', icone: '◆' },
  { id: 'diario', rotulo: 'DIÁRIO', icone: '◷' },
  { id: 'cadastro', rotulo: 'CADASTRO', icone: '⊕' },
];

const EM_ESPERA = {
  fatura: {
    olho: 'CARTÃO // O CICLO',
    titulo: 'Fatura',
    responde: 'O ciclo do cartão e as ações de fechar, pagar e abrir — esta última só na última fatura fechada. O que vence sem ser pago rola para a seguinte.',
    falta: ['conta CARTAO', 'fatura'],
  },
  series: {
    olho: 'SÉRIES // O QUE SE REPETE',
    titulo: 'Séries',
    responde: 'Os parcelamentos e as recorrências vivos, e o que muda ao alterar cada um. Parcelamento altera todas as parcelas sempre; recorrência pergunta se é só o futuro ou o passado também.',
    falta: ['parcelamento', 'recorrência'],
  },
  reserva: {
    olho: 'PATRIMÔNIO // FORA DO CAIXA',
    titulo: 'Reserva',
    responde: 'Contas, aplicações e a diferença entre fluxo de caixa e patrimônio. É onde o valor informado de uma aplicação mostra a idade dele — o sistema nunca extrapola rendimento.',
    falta: ['valor informado da aplicação', 'a tela de patrimônio'],
  },
  diario: {
    olho: 'DIÁRIO // FASE 2',
    titulo: 'Diário',
    responde: 'O que aconteceu num dia: o que o sistema fez sozinho e o que a pessoa fez. Toda outra tela mostra como as coisas estão; esta é a única que mostra o que aconteceu.',
    falta: ['evento', 'fatura'],
  },
};

const seletor = (id) => document.querySelector(`[data-tela="${id}"]`);

function montarRail() {
  document.getElementById('rail').innerHTML =
    TELAS.map((t) => `<a class="nav${EM_ESPERA[t.id] ? ' vazia' : ''}" href="#/${t.id}"
        data-nav="${t.id}"><span>${t.icone}</span>${t.rotulo}</a>`).join('')
    + '<div class="railfoot tele">CB<br>NC77</div>';
}

function montarEmEspera() {
  Object.entries(EM_ESPERA).forEach(([id, tela]) => {
    seletor(id).innerHTML = `
      <div class="telahead">
        <div><div class="eyebrow">${tela.olho}</div><h2 class="th">${tela.titulo}</h2></div>
        <div class="tele">AINDA NÃO EXISTE</div>
      </div>
      <div class="panel">
        <div class="emespera">
          <h4>O que esta tela vai responder</h4>
          <p>${tela.responde}</p>
          <div class="falta">FALTA PARA ELA EXISTIR: ${tela.falta.map((f) => `<b>${f}</b>`).join(' · ')}</div>
        </div>
      </div>`;
  });
}

const ABA_DO_CADASTRO = { categorias: () => Cadastro.montar(), contas: () => Contas.montar() };

const AO_ENTRAR = {
  home: () => Home.montar(),
  extrato: () => Extrato.montar(),
  cadastro: () => abrirAba(document.querySelector('#abasCadastro .aba.on').dataset.aba),
};

function abrirAba(nome) {
  document.querySelectorAll('#abasCadastro .aba').forEach((botao) => {
    botao.classList.toggle('on', botao.dataset.aba === nome);
  });
  document.querySelectorAll('[data-painel]').forEach((painel) => {
    painel.classList.toggle('hidden', painel.dataset.painel !== nome);
  });
  return ABA_DO_CADASTRO[nome]();
}

function irPara(id) {
  const destino = TELAS.some((t) => t.id === id) ? id : 'home';

  TELAS.forEach((t) => {
    seletor(t.id).classList.toggle('hidden', t.id !== destino);
    document.querySelector(`[data-nav="${t.id}"]`).classList.toggle('on', t.id === destino);
  });

  document.getElementById('main').scrollTop = 0;
  document.title = `CYBERBANK // ${destino.toUpperCase()}`;

  if (AO_ENTRAR[destino]) AO_ENTRAR[destino]();
}

function trocarPeloHash() {
  irPara((window.location.hash || '').replace(/^#\/?/, ''));
}

function tratarFalha(erro) {
  if (erro.codigo === 'NAO_AUTENTICADO') {
    window.location.href = 'login.html';
    return true;
  }
  return false;
}

async function iniciar() {
  let ambientes;
  try {
    ambientes = await API.listarAmbientes();
  } catch (erro) {
    window.location.href = 'login.html';
    return;
  }

  Contexto.ambiente = ambientes.itens[0];
  document.getElementById('ambienteNome').textContent = Contexto.ambiente.nome;
  document.getElementById('ambientePapel').textContent = Contexto.ambiente.papel;
  document.getElementById('quemSou').textContent = `NÓ ${Contexto.ambiente.id}`;
  document.getElementById('avatar').textContent = Contexto.ambiente.nome.charAt(0).toUpperCase();

  montarRail();
  montarEmEspera();

  document.getElementById('abasCadastro').addEventListener('click', async (evento) => {
    const botao = evento.target.closest('[data-aba]');
    if (botao) await abrirAba(botao.dataset.aba);
  });

  window.addEventListener('hashchange', trocarPeloHash);
  trocarPeloHash();
}

document.getElementById('btnSair').addEventListener('click', async () => {
  try { await API.sair(); } catch (erro) { void erro; }
  window.location.href = 'login.html';
});

iniciar();
