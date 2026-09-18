const Contexto = { ambiente: null, usuario: null };

const TELAS = [
  { id: 'home', rotulo: 'HOME', icone: '◈' },
  { id: 'extrato', rotulo: 'EXTRATO', icone: '≡' },
  { id: 'fatura', rotulo: 'FATURA', icone: '▤' },
  { id: 'series', rotulo: 'SÉRIES', icone: '∞' },
  { id: 'reserva', rotulo: 'RESERVA', icone: '◆' },
  { id: 'diario', rotulo: 'DIÁRIO', icone: '◷' },
  { id: 'cadastro', rotulo: 'CADASTRO', icone: '⊕' },
  { id: 'perfil', rotulo: 'PERFIL', icone: '◉', foraDoRail: true },
];

const EM_ESPERA = {
  series: {
    olho: 'SÉRIES // O QUE SE REPETE',
    titulo: 'Séries',
    responde: 'Os parcelamentos e as recorrências vivos, e o que muda ao alterar cada um. Parcelamento altera todas as parcelas sempre; recorrência pergunta se é só o futuro ou o passado também.',
    falta: ['parcelamento', 'recorrência'],
  },
};

const seletor = (id) => document.querySelector(`[data-tela="${id}"]`);

function montarRail() {
  document.getElementById('rail').innerHTML =
    TELAS.filter((t) => !t.foraDoRail)
      .map((t) => `<a class="nav${EM_ESPERA[t.id] ? ' vazia' : ''}" href="#/${t.id}"
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
  extrato: (lancamentoId) => Extrato.montar(lancamentoId),
  fatura: (cartaoId) => Fatura.montar(cartaoId),
  reserva: () => Reserva.montar(),
  diario: () => Diario.montar(),
  cadastro: () => abrirAba(document.querySelector('#abasCadastro .aba.on').dataset.aba),
  perfil: () => Perfil.montar(),
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

function irPara(id, argumento) {
  const destino = TELAS.some((t) => t.id === id) ? id : 'home';

  TELAS.forEach((t) => {
    seletor(t.id).classList.toggle('hidden', t.id !== destino);
    const atalho = document.querySelector(`[data-nav="${t.id}"]`);
    if (atalho) atalho.classList.toggle('on', t.id === destino);
  });

  document.getElementById('avatar').classList.toggle('on', destino === 'perfil');

  if (destino !== 'extrato') Detalhe.esconder();

  document.getElementById('main').scrollTop = 0;
  document.title = `CYBERBANK // ${destino.toUpperCase()}`;

  if (AO_ENTRAR[destino]) AO_ENTRAR[destino](argumento);
}

async function recarregarTelaAtual() {
  const atual = TELAS.find((t) => !seletor(t.id).classList.contains('hidden'));
  if (atual && AO_ENTRAR[atual.id]) await AO_ENTRAR[atual.id]();
}

function trocarPeloHash() {
  const [tela, argumento] = (window.location.hash || '').replace(/^#\/?/, '').split('/');
  irPara(tela, argumento);
}

function atualizarIdentidade(usuario) {
  Contexto.usuario = usuario;
  document.getElementById('quemSou').textContent = usuario.nome;
  document.getElementById('avatar').innerHTML = Avatares.svg(usuario.avatar);
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

  try {
    atualizarIdentidade(await API.verPerfil());
  } catch (erro) {
    if (tratarFalha(erro)) return;
  }

  montarRail();
  montarEmEspera();
  Lancar.ligar();

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
