/* =========================================================================
   CYBERBANK // acesso: entrar e provisionar identidade
   ========================================================================= */

const tela = {
  login: document.getElementById('viewLogin'),
  cadastro: document.getElementById('viewCadastro'),
};

function mostrar(qual) {
  tela.login.classList.toggle('hidden', qual !== 'login');
  tela.cadastro.classList.toggle('hidden', qual !== 'cadastro');
  limparMensagens();
}

document.querySelectorAll('[data-ir]').forEach((botao) => {
  botao.addEventListener('click', () => mostrar(botao.dataset.ir));
});

function limparMensagens() {
  ['msgLogin', 'msgCadastro'].forEach((id) => {
    const alvo = document.getElementById(id);
    alvo.textContent = '';
    alvo.className = 'msg';
  });
}

function dizer(id, texto, tipo) {
  const alvo = document.getElementById(id);
  alvo.textContent = texto;
  alvo.className = `msg ${tipo}`;
}

/** Enquanto a requisicao esta no ar o botao para de aceitar clique — senao vira duas sessoes. */
async function enviando(botao, rotulo, acao) {
  const original = botao.textContent;
  botao.disabled = true;
  botao.textContent = rotulo;
  try {
    await acao();
  } finally {
    botao.disabled = false;
    botao.textContent = original;
  }
}

if (new URLSearchParams(window.location.search).has('senhaTrocada')) {
  dizer('msgLogin', 'Senha trocada. Entre de novo — a troca encerra todas as sessões.', 'ok');
}

if (new URLSearchParams(window.location.search).has('sessoesEncerradas')) {
  dizer('msgLogin', 'Todas as sessões foram encerradas, inclusive a sua. Entre de novo.', 'ok');
}

document.getElementById('fLogin').addEventListener('submit', (evento) => {
  evento.preventDefault();
  limparMensagens();
  const botao = document.getElementById('btnLogin');

  enviando(botao, 'Autenticando…', async () => {
    try {
      await API.entrar(
        document.getElementById('loginEmail').value.trim(),
        document.getElementById('loginSenha').value,
      );
      window.location.href = 'app.html';
    } catch (erro) {
      dizer('msgLogin', erro.paraGente(), 'err');
    }
  });
});

document.getElementById('fCadastro').addEventListener('submit', (evento) => {
  evento.preventDefault();
  limparMensagens();
  const botao = document.getElementById('btnCadastro');
  const email = document.getElementById('cadEmail').value.trim();
  const senha = document.getElementById('cadSenha').value;

  enviando(botao, 'Provisionando…', async () => {
    try {
      await API.cadastrarUsuario(document.getElementById('cadNome').value.trim(), email, senha);
      // Cadastrar nao abre sessao: o login e um passo proprio, e e ele que poe o cookie.
      await API.entrar(email, senha);
      window.location.href = 'app.html';
    } catch (erro) {
      dizer('msgCadastro', erro.paraGente(), 'err');
    }
  });
});

/* ---------- a telemetria da lateral: moldura, nunca conteudo ---------- */

const LINHAS_DE_BOOT = [
  'BOOT <b>OK</b>',
  'NODE  cyberbank/local',
  'CRYPT argon2id <b>ATIVO</b>',
  'RLS   por ambiente <b>ARMADO</b>',
  'SESS  cookie httpOnly',
  'AGUARDANDO IDENTIDADE <span class="cur">_</span>',
];

const painelDeBoot = document.getElementById('boot');
if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
  painelDeBoot.innerHTML = LINHAS_DE_BOOT.join('<br>');
} else {
  let i = 0;
  const passo = setInterval(() => {
    painelDeBoot.innerHTML = LINHAS_DE_BOOT.slice(0, ++i).join('<br>');
    if (i >= LINHAS_DE_BOOT.length) clearInterval(passo);
  }, 160);
}

/* Sessao viva nao passa pela porta: quem ja entrou vai direto para o terminal. */
API.listarAmbientes()
  .then(() => { window.location.href = 'app.html'; })
  .catch(() => { /* sem sessao e o caso normal desta tela */ });
