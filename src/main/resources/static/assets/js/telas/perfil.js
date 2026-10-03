const Perfil = {
  usuario: null,
  avatarEscolhido: null,
  ambientes: [],
  renomeando: null,
  sessoes: [],
  convites: [],
  pessoasDe: null,
  membros: [],
  convitesEnviados: [],
  papelDoConvite: 'EDITOR',
  confirmando: null,
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
    await Promise.all([
      Perfil.carregarAmbientes(), Perfil.carregarSessoes(), Perfil.carregarConvites(),
    ]);
  },

  async carregarConvites() {
    try {
      Perfil.convites = (await API.convitesRecebidos()).itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoConvites', erro.paraGente(), 'err');
      return;
    }
    Perfil.pintarConvites();
  },

  pintarConvites() {
    const total = Perfil.convites.length;
    document.getElementById('perfilConvitesConta').textContent =
      total ? `${total} ESPERANDO` : 'NENHUM';

    document.getElementById('perfilConvites').innerHTML = total
      ? Perfil.convites.map((convite) => `
        <article class="conta">
          <div class="conta-head">
            <div class="conta-nome">
              <strong>${Formato.texto(convite.ambienteNome)}</strong>
              <span class="tag">${Formato.papel(convite.papel)}</span>
            </div>
            <div class="hstack gap6">
              <button class="btn sm primary" type="button" data-convite="${convite.id}" data-resposta="aceitar">Aceitar</button>
              <button class="btn sm ghost" type="button" data-convite="${convite.id}" data-resposta="recusar">Recusar</button>
            </div>
          </div>
          <div class="conta-ciclo">
            <span class="tele">CONVIDADO POR ${Formato.texto(convite.convidadoPor)}</span>
            <span class="tele">EM ${Perfil.instante(convite.criadoEm)}</span>
          </div>
          <p class="explica-papel">${Perfil.EXPLICA_PAPEL[convite.papel]}</p>
        </article>`).join('')
      : '<div class="vazio">Nenhum convite esperando por você.</div>';
  },

  EXPLICA_PAPEL: {
    EDITOR: 'Autorização completa: lança, corrige e cadastra conta, categoria e meio. Não convida ninguém nem exclui o ambiente.',
    LEITOR: 'Somente leitura: vê tudo do ambiente e não muda nada.',
  },

  async responderConvite(id, resposta) {
    const convite = Perfil.convites.find((c) => c.id === id);
    if (resposta === 'aceitar') {
      await Perfil.tentar('avisoConvites',
        `Você entrou em ${convite ? convite.ambienteNome : 'um ambiente'}. Ele já está no seletor do topo.`,
        async () => {
          await API.aceitarConvite(id);
          await Promise.all([Perfil.carregarConvites(), Perfil.carregarAmbientes()]);
        });
      return;
    }
    await Perfil.tentar('avisoConvites', 'Convite recusado.', async () => {
      await API.recusarConvite(id);
      await Perfil.carregarConvites();
    });
  },

  async abrirPessoas(ambienteId) {
    if (Perfil.pessoasDe === ambienteId) {
      Perfil.pessoasDe = null;
      Perfil.pintarAmbientes();
      return;
    }
    Perfil.pessoasDe = ambienteId;
    Perfil.confirmando = null;
    Perfil.papelDoConvite = 'EDITOR';
    await Perfil.carregarPessoas();
  },

  async carregarPessoas() {
    const ambiente = Perfil.ambientes.find((a) => a.id === Perfil.pessoasDe);
    if (!ambiente) return;
    try {
      const [membros, convites] = await Promise.all([
        API.membros(ambiente.id),
        ambiente.papel === 'DONO' ? API.convitesDoAmbiente(ambiente.id) : { itens: [] },
      ]);
      Perfil.membros = membros.itens;
      Perfil.convitesEnviados = convites.itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoAmbientes', erro.paraGente(), 'err');
      return;
    }
    Perfil.pintarAmbientes();
  },

  pintarPessoas(ambiente) {
    const dono = ambiente.papel === 'DONO';
    return `
      <div class="pessoas">
        <div id="avisoPessoas"></div>
        <div class="tele pessoas-titulo">QUEM TEM ACESSO</div>
        ${Perfil.membros.map((membro) => Perfil.pintarMembro(ambiente, membro, dono)).join('')}
        ${dono ? Perfil.pintarConvitesEnviados(ambiente) : ''}
      </div>`;
  },

  pintarMembro(ambiente, membro, dono) {
    const chave = `${ambiente.id}:${membro.usuarioId}`;
    const removivel = membro.papel !== 'DONO' && (dono || membro.voce);
    let acao = '';
    if (removivel && Perfil.confirmando === chave) {
      acao = `
        <button class="btn sm danger" type="button" data-acao="confirmar-remocao" data-id="${ambiente.id}" data-usuario="${membro.usuarioId}">${membro.voce ? 'Confirmar saída' : 'Confirmar remoção'}</button>
        <button class="btn sm ghost" type="button" data-acao="desistir" data-id="${ambiente.id}">Desistir</button>`;
    } else if (removivel) {
      acao = `<button class="btn sm ghost" type="button" data-acao="remover" data-id="${ambiente.id}" data-usuario="${membro.usuarioId}">${membro.voce ? 'Sair do ambiente' : 'Remover'}</button>`;
    }
    return `
      <div class="pessoa">
        <span class="pessoa-av">${Avatares.svg(membro.avatar)}</span>
        <div class="pessoa-id">
          <strong>${Formato.texto(membro.nome)}${membro.voce ? ' <i>· você</i>' : ''}</strong>
          <span class="tele">${Formato.texto(membro.email)}</span>
        </div>
        <span class="tag">${Formato.papel(membro.papel)}</span>
        <div class="hstack gap6">${acao}</div>
      </div>
      ${Perfil.confirmando === chave ? `<p class="explica-papel">${membro.voce
        ? 'Você deixa de ver este ambiente. O que você lançou continua nele — o dado é do ambiente.'
        : 'A pessoa deixa de ver o ambiente. O que ela lançou continua nele, com o nome dela.'}</p>` : ''}`;
  },

  pintarConvitesEnviados(ambiente) {
    const pendentes = Perfil.convitesEnviados.map((convite) => `
      <div class="pessoa">
        <span class="pessoa-av vazio-av">?</span>
        <div class="pessoa-id">
          <strong>${Formato.texto(convite.email)}</strong>
          <span class="tele">CONVITE ESPERANDO RESPOSTA</span>
        </div>
        <span class="tag">${Formato.papel(convite.papel)}</span>
        <div class="hstack gap6">
          <button class="btn sm ghost" type="button" data-acao="cancelar-convite" data-id="${ambiente.id}" data-convite="${convite.id}">Cancelar convite</button>
        </div>
      </div>`).join('');

    return `
      ${pendentes}
      <div class="tele pessoas-titulo">CONVIDAR PARA ${Formato.texto(ambiente.nome).toUpperCase()}</div>
      <div class="field">
        <label for="conviteEmail">E-mail de quem você quer convidar</label>
        <input id="conviteEmail" type="email" maxlength="255" placeholder="pessoa@exemplo.com" autocomplete="off">
      </div>
      <div class="seg" role="group" aria-label="Autorização">
        <button type="button" class="${Perfil.papelDoConvite === 'EDITOR' ? 'on' : ''}" data-acao="papel-convite" data-id="${ambiente.id}" data-papel="EDITOR" aria-pressed="${Perfil.papelDoConvite === 'EDITOR'}">Autorização completa</button>
        <button type="button" class="${Perfil.papelDoConvite === 'LEITOR' ? 'on' : ''}" data-acao="papel-convite" data-id="${ambiente.id}" data-papel="LEITOR" aria-pressed="${Perfil.papelDoConvite === 'LEITOR'}">Somente leitura</button>
      </div>
      <p class="explica-papel">${Perfil.EXPLICA_PAPEL[Perfil.papelDoConvite]}</p>
      <div class="hstack gap10">
        <button class="btn primary" type="button" data-acao="convidar" data-id="${ambiente.id}">Convidar</button>
      </div>`;
  },

  async convidar(ambienteId) {
    const campo = document.getElementById('conviteEmail');
    const email = campo.value.trim();
    try {
      await API.convidar(ambienteId, email, Perfil.papelDoConvite);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoPessoas', erro.paraGente(), 'err');
      return;
    }
    await Perfil.carregarPessoas();
    Perfil.avisar('avisoPessoas',
      `Convite feito. Ele aparece para ${email} no Perfil, assim que a pessoa entrar — ou se cadastrar com esse e-mail.`,
      'ok');
  },

  async cancelarConvite(ambienteId, conviteId) {
    try {
      await API.cancelarConvite(ambienteId, conviteId);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoPessoas', erro.paraGente(), 'err');
      return;
    }
    await Perfil.carregarPessoas();
    Perfil.avisar('avisoPessoas', 'Convite cancelado.', 'ok');
  },

  async remover(ambienteId, usuarioId) {
    const saindo = usuarioId === Perfil.usuario.id;
    try {
      await API.removerMembro(ambienteId, usuarioId);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoPessoas', erro.paraGente(), 'err');
      return;
    }
    Perfil.confirmando = null;
    if (saindo && Contexto.ambiente && Contexto.ambiente.id === ambienteId) {
      window.location.reload();
      return;
    }
    if (saindo) {
      Perfil.pessoasDe = null;
      await Perfil.carregarAmbientes();
      Perfil.avisar('avisoAmbientes', 'Você saiu do ambiente.', 'ok');
      return;
    }
    await Perfil.carregarPessoas();
    Perfil.avisar('avisoPessoas', 'Acesso removido.', 'ok');
  },

  async carregarSessoes() {
    try {
      Perfil.sessoes = (await API.listarSessoes()).itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoSessoes', erro.paraGente(), 'err');
      return;
    }
    Perfil.pintarSessoes();
  },

  pintarSessoes() {
    const total = Perfil.sessoes.length;
    document.getElementById('perfilSessoesConta').textContent =
      `${total} ${total === 1 ? 'ABERTA' : 'ABERTAS'}`;

    document.getElementById('perfilSessoes').innerHTML = Perfil.sessoes
      .map((sessao) => `
        <article class="conta">
          <div class="conta-head">
            <div class="conta-nome">
              <strong title="${Formato.texto(sessao.navegador || '')}">${Formato.texto(Perfil.aparelho(sessao.navegador))}</strong>
              ${sessao.atual ? '<span class="tag real">ESTA SESSÃO</span>' : ''}
            </div>
            <button class="btn sm ${sessao.atual ? 'ghost' : 'danger'}" type="button"
                    data-sessao="${sessao.id}" data-atual="${sessao.atual}">${sessao.atual ? 'Sair' : 'Encerrar'}</button>
          </div>
          <div class="conta-ciclo">
            <span class="tele">DE ${Formato.texto(sessao.origem || 'ORIGEM DESCONHECIDA')}</span>
            <span class="tele">ENTROU ${Perfil.instante(sessao.criadaEm)}</span>
            <span class="tele">ÚLTIMO USO ${Perfil.instante(sessao.ultimoUsoEm)}</span>
          </div>
        </article>`)
      .join('');
  },

  aparelho(navegador) {
    if (!navegador) return 'Navegador desconhecido';
    const qual = [
      [/Edg\//, 'Edge'], [/OPR\//, 'Opera'], [/Firefox\//, 'Firefox'],
      [/Chrome\//, 'Chrome'], [/Safari\//, 'Safari'],
    ].find(([padrao]) => padrao.test(navegador));
    const onde = [
      [/Android/, 'Android'], [/iPhone|iPad/, 'iOS'], [/CrOS/, 'ChromeOS'],
      [/Windows/, 'Windows'], [/Mac OS X/, 'macOS'], [/Linux/, 'Linux'],
    ].find(([padrao]) => padrao.test(navegador));
    if (!qual && !onde) return 'Navegador desconhecido';
    return [qual ? qual[1] : 'Navegador', onde ? onde[1] : null].filter(Boolean).join(' · ');
  },

  instante(iso) {
    const quando = new Date(iso);
    const dia = quando.toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' });
    const hora = quando.toLocaleTimeString('pt-BR',
      { timeZone: 'America/Sao_Paulo', hour: '2-digit', minute: '2-digit' });
    return `${dia} ${hora}`;
  },

  async encerrarSessao(id, atual) {
    try {
      await API.encerrarSessao(id);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoSessoes', erro.paraGente(), 'err');
      return;
    }
    if (atual) {
      window.location.href = 'login.html';
      return;
    }
    Perfil.avisar('avisoSessoes', 'Sessão encerrada. Aquele aparelho sai no próximo clique.', 'ok');
    await Perfil.carregarSessoes();
  },

  async encerrarTodasAsSessoes() {
    try {
      await API.encerrarTodasAsSessoes();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoSessoes', erro.paraGente(), 'err');
      return;
    }
    window.location.href = 'login.html?sessoesEncerradas=1';
  },

  async carregarAmbientes() {
    try {
      Perfil.ambientes = (await API.listarAmbientes()).itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Perfil.avisar('avisoAmbientes', erro.paraGente(), 'err');
      return;
    }
    Perfil.pintarAmbientes();
  },

  pintarAmbientes() {
    const total = Perfil.ambientes.length;
    document.getElementById('perfilAmbientesConta').textContent =
      `${total} ${total === 1 ? 'AMBIENTE' : 'AMBIENTES'}`;

    document.getElementById('perfilAmbientes').innerHTML = Perfil.ambientes
      .map((ambiente) => `
        <article class="conta">
          <div class="conta-head">
            ${Perfil.renomeando === ambiente.id ? `
              <div class="conta-edicao">
                <input id="ambienteNome${ambiente.id}" type="text" maxlength="80"
                       value="${Formato.texto(ambiente.nome)}">
                <button class="btn sm primary" type="button" data-acao="salvar" data-id="${ambiente.id}">Salvar</button>
                <button class="btn sm ghost" type="button" data-acao="cancelar" data-id="${ambiente.id}">Cancelar</button>
              </div>`
            : `
              <div class="conta-nome">
                <strong>${Formato.texto(ambiente.nome)}</strong>
                <span class="tag">${Formato.papel(ambiente.papel)}</span>
              </div>
              <div class="hstack gap6">
                <button class="btn sm ${Perfil.pessoasDe === ambiente.id ? 'primary' : 'ghost'}" type="button" data-acao="pessoas" data-id="${ambiente.id}" aria-expanded="${Perfil.pessoasDe === ambiente.id}">Pessoas</button>
                ${ambiente.papel === 'LEITOR' ? ''
                  : `<button class="btn sm ghost" type="button" data-acao="renomear" data-id="${ambiente.id}">Renomear</button>`}
              </div>`}
          </div>
          <div class="conta-ciclo">
            <span class="tele">CRIADO EM ${new Date(ambiente.criadoEm).toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' })}</span>
          </div>
          ${Perfil.pessoasDe === ambiente.id ? Perfil.pintarPessoas(ambiente) : ''}
        </article>`)
      .join('');

    if (Perfil.renomeando) {
      document.getElementById(`ambienteNome${Perfil.renomeando}`)?.focus();
    }
  },

  async criarAmbiente() {
    const campo = document.getElementById('perfilAmbienteNovo');
    const nome = campo.value.trim();
    await Perfil.tentar('avisoAmbientes', 'Ambiente criado.', async () => {
      await API.criarAmbiente(nome);
      campo.value = '';
      await Perfil.carregarAmbientes();
    });
  },

  async renomearAmbiente(id) {
    const nome = document.getElementById(`ambienteNome${id}`).value.trim();
    await Perfil.tentar('avisoAmbientes', 'Ambiente renomeado.', async () => {
      const renomeado = await API.renomearAmbiente(id, nome);
      Perfil.renomeando = null;
      if (Contexto.ambiente && Contexto.ambiente.id === renomeado.id) {
        Contexto.ambiente.nome = renomeado.nome;
        document.getElementById('ambienteNome').textContent = renomeado.nome;
      }
      await Perfil.carregarAmbientes();
    });
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

    document.getElementById('perfilAmbienteCriar').addEventListener('click', Perfil.criarAmbiente);
    document.getElementById('perfilSessoesTodas').addEventListener('click', Perfil.encerrarTodasAsSessoes);
    document.getElementById('perfilSessoes').addEventListener('click', (evento) => {
      const botao = evento.target.closest('[data-sessao]');
      if (botao) Perfil.encerrarSessao(Number(botao.dataset.sessao), botao.dataset.atual === 'true');
    });
    document.getElementById('perfilAmbienteNovo').addEventListener('keydown', (evento) => {
      if (evento.key === 'Enter') Perfil.criarAmbiente();
    });
    document.getElementById('perfilAmbientes').addEventListener('click', (evento) => {
      const botao = evento.target.closest('[data-acao]');
      if (!botao) return;
      const id = Number(botao.dataset.id);
      if (botao.dataset.acao === 'renomear') {
        Perfil.renomeando = id;
        Perfil.pintarAmbientes();
      } else if (botao.dataset.acao === 'cancelar') {
        Perfil.renomeando = null;
        Perfil.pintarAmbientes();
      } else if (botao.dataset.acao === 'salvar') {
        Perfil.renomearAmbiente(id);
      } else if (botao.dataset.acao === 'pessoas') {
        Perfil.abrirPessoas(id);
      } else if (botao.dataset.acao === 'papel-convite') {
        const email = document.getElementById('conviteEmail').value;
        Perfil.papelDoConvite = botao.dataset.papel;
        Perfil.pintarAmbientes();
        document.getElementById('conviteEmail').value = email;
      } else if (botao.dataset.acao === 'convidar') {
        Perfil.convidar(id);
      } else if (botao.dataset.acao === 'cancelar-convite') {
        Perfil.cancelarConvite(id, Number(botao.dataset.convite));
      } else if (botao.dataset.acao === 'remover') {
        Perfil.confirmando = `${id}:${botao.dataset.usuario}`;
        Perfil.pintarAmbientes();
      } else if (botao.dataset.acao === 'desistir') {
        Perfil.confirmando = null;
        Perfil.pintarAmbientes();
      } else if (botao.dataset.acao === 'confirmar-remocao') {
        Perfil.remover(id, Number(botao.dataset.usuario));
      }
    });
    document.getElementById('perfilConvites').addEventListener('click', (evento) => {
      const botao = evento.target.closest('[data-resposta]');
      if (botao) Perfil.responderConvite(Number(botao.dataset.convite), botao.dataset.resposta);
    });
    document.getElementById('perfilAmbientes').addEventListener('keydown', (evento) => {
      if (evento.key === 'Enter' && evento.target.id.startsWith('ambienteNome')) {
        Perfil.renomearAmbiente(Perfil.renomeando);
      } else if (evento.key === 'Enter' && evento.target.id === 'conviteEmail') {
        Perfil.convidar(Perfil.pessoasDe);
      } else if (evento.key === 'Escape') {
        Perfil.renomeando = null;
        Perfil.pintarAmbientes();
      }
    });
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
