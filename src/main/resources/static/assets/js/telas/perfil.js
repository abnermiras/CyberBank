const Perfil = {
  usuario: null,
  avatarEscolhido: null,
  ligado: false,

  async montar() {
    Perfil.ligar();
    try {
      Perfil.usuario = await API.verPerfil();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoPerfil', erro.paraGente(), 'err');
      return;
    }
    Perfil.avatarEscolhido = Perfil.usuario.avatar;
    Perfil.pintar();
  },

  pintar() {
    const eu = Perfil.usuario;

    document.getElementById('perfilAvatares').innerHTML = eu.avataresDisponiveis
      .map((nome) => `<button type="button" class="av-op${nome === Perfil.avatarEscolhido ? ' on' : ''}"
             data-avatar="${nome}" title="${nome}" aria-pressed="${nome === Perfil.avatarEscolhido}"
             >${Avatares.svg(nome)}<i>${nome}</i></button>`)
      .join('');

    document.getElementById('perfilNome').value = eu.nome;
    document.getElementById('perfilEmail').value = eu.email;
    document.getElementById('perfilDesde').textContent = Formato.dia(eu.criadoEm.slice(0, 10));

    document.getElementById('perfilTelegram').value = eu.telegramChatId ?? '';
    document.getElementById('perfilTelegramTirar').classList.toggle('hidden', !eu.telegramChatId);
    document.getElementById('perfilTelegramEstado').textContent = eu.telegramChatId
      ? 'VINCULADO · NADA É ENVIADO ATÉ O BOT EXISTIR'
      : 'SEM VÍNCULO';

    atualizarIdentidade(eu);
  },

  ligar() {
    if (Perfil.ligado) return;
    Perfil.ligado = true;

    document.getElementById('perfilAvatares').addEventListener('click', (evento) => {
      const opcao = evento.target.closest('[data-avatar]');
      if (!opcao) return;
      Perfil.avatarEscolhido = opcao.dataset.avatar;
      Perfil.pintar();
    });

    document.getElementById('perfilSalvar').addEventListener('click', Perfil.salvarIdentidade);
    document.getElementById('perfilTelegramSalvar').addEventListener('click', Perfil.vincular);
    document.getElementById('perfilTelegramTirar').addEventListener('click', Perfil.desvincular);
    document.getElementById('perfilSenhaTrocar').addEventListener('click', Perfil.trocarSenha);
  },

  async salvarIdentidade() {
    const nome = document.getElementById('perfilNome').value.trim();
    await Perfil.tentar('avisoPerfil', 'Identidade atualizada.', async () => {
      Perfil.usuario = await API.alterarPerfil({ nome, avatar: Perfil.avatarEscolhido });
      Perfil.avatarEscolhido = Perfil.usuario.avatar;
      Perfil.pintar();
    });
  },

  async vincular() {
    const digitado = document.getElementById('perfilTelegram').value.trim();
    const chatId = Number(digitado);
    if (!digitado || !Number.isInteger(chatId)) {
      Perfil.avisar('avisoTelegram', 'O id do chat é um número inteiro.', 'err');
      return;
    }
    await Perfil.tentar('avisoTelegram', 'Chat vinculado.', async () => {
      Perfil.usuario = await API.vincularTelegram(chatId);
      Perfil.pintar();
    });
  },

  async desvincular() {
    await Perfil.tentar('avisoTelegram', 'Vínculo desfeito.', async () => {
      await API.desvincularTelegram();
      Perfil.usuario = await API.verPerfil();
      Perfil.pintar();
    });
  },

  async trocarSenha() {
    const atual = document.getElementById('senhaAtual').value;
    const nova = document.getElementById('senhaNova').value;
    const repetida = document.getElementById('senhaRepetida').value;

    if (nova !== repetida) {
      Perfil.avisar('avisoSenha', 'A nova senha e a repetição não conferem.', 'err');
      return;
    }

    try {
      await API.trocarSenha(atual, nova);
    } catch (erro) {
      Perfil.avisar('avisoSenha', erro.paraGente(), 'err');
      return;
    }
    window.location.href = 'login.html?senhaTrocada=1';
  },

  async tentar(aviso, sucesso, acao) {
    try {
      await acao();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar(aviso, erro.paraGente(), 'err');
      return;
    }
    Perfil.avisar(aviso, sucesso, 'ok');
  },

  avisar(id, texto, tipo) {
    document.getElementById(id).innerHTML =
      `<div class="aviso ${tipo}">${Formato.texto(texto)}</div>`;
  },
};
