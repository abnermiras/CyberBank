const Reserva = {
  dados: null,
  informando: null,
  movendo: null,
  ligado: false,

  async montar() {
    if (!Reserva.ligado) {
      Reserva.ligarOuvintes();
      Reserva.ligado = true;
    }
    await Reserva.recarregar();
  },

  async recarregar() {
    try {
      Reserva.dados = await API.reserva(Contexto.ambiente.id);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Reserva.avisar(erro.paraGente());
      return;
    }
    Reserva.desenhar();
  },

  ligarOuvintes() {
    document.getElementById('reservaCorpo').addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-reserva]');
      if (!botao) return;
      const acoes = {
        informar: () => Reserva.abrir('informando', Number(botao.dataset.id)),
        aportar: () => Reserva.abrir('movendo', Number(botao.dataset.id), 'APORTE'),
        resgatar: () => Reserva.abrir('movendo', Number(botao.dataset.id), 'RESGATE'),
        desistir: () => Reserva.abrir(null, null),
        gravar: () => Reserva.gravar(Number(botao.dataset.id)),
        mover: () => Reserva.mover(Number(botao.dataset.id)),
      };
      await acoes[botao.dataset.reserva]();
    });

    document.getElementById('reservaCorpo').addEventListener('submit', (evento) => {
      evento.preventDefault();
      Reserva.gravar(Reserva.informando);
    });
  },

  abrir(campo, id, operacao) {
    Reserva.informando = campo === 'informando' ? id : null;
    Reserva.movendo = campo === 'movendo' ? { id, operacao } : null;
    Reserva.desenhar();
  },

  desenhar() {
    const d = Reserva.dados;

    document.getElementById('reservaCorpo').innerHTML = `
      <div id="avisoReserva"></div>

      <div class="grid3">
        <div class="panel metric">
          <span class="lbl">Guardado</span>
          <span class="val lm">${Formato.dinheiro(d.guardadoCentavos)}</span>
          <span class="sub">fora do fluxo de caixa</span>
        </div>
        <div class="panel metric">
          <span class="lbl">Em caixa</span>
          <span class="val cy">${Formato.dinheiro(d.emCaixaCentavos)}</span>
          <span class="sub">paga qualquer coisa</span>
        </div>
        <div class="panel metric">
          <span class="lbl">Patrimônio</span>
          <span class="val">${Formato.dinheiro(d.patrimonioCentavos)}</span>
          <span class="sub">${d.algumaDesatualizada
            ? 'contém valor desatualizado' : 'todas as contas'}</span>
        </div>
      </div>

      <section class="panel mt18">
        <div class="panel-head">
          <h3>Aplicações</h3>
          <span class="contagem">${d.aplicacoes.length
            ? `${d.aplicacoes.length} ${d.aplicacoes.length > 1 ? 'APLICAÇÕES' : 'APLICAÇÃO'}`
            : 'NENHUMA'}</span>
        </div>
        ${d.aplicacoes.length
          ? d.aplicacoes.map(Reserva.linha).join('')
          : `<div class="vazio">Nenhuma aplicação ainda. Abra uma conta do tipo
              <b>APLICACAO</b> no Cadastro — poupança, CDB e reserva de emergência são
              todas aplicações, e o que muda entre elas é o nome.</div>`}
        <p class="rodape-regra">O sistema <b>nunca extrapola</b> o valor de uma aplicação: não
          há rendimento estimado nem curva projetada. O número é o último que você informou, e
          a idade dele fica à vista — número velho com a idade ao lado é honesto; apresentado
          como atual, não.</p>
      </section>

      <section class="panel mt18">
        <div class="panel-head"><h3>Fluxo de caixa não é patrimônio</h3></div>
        <p class="rodape-regra">Um <b>aporte</b> muda o fluxo de caixa do mês e
          <b>não muda o patrimônio</b>: o dinheiro só trocou de bolso. É por isso que guardar
          dinheiro não aparece como gasto no relatório por categoria — e por isso o mês deixa
          de fechar na soma simples, com a linha <b>guardado</b> separada na Home.
          Só <b>rendimento</b> muda o patrimônio.</p>
      </section>`;
  },

  linha(a) {
    const informando = Reserva.informando === a.id;
    const movendo = Reserva.movendo && Reserva.movendo.id === a.id
      ? Reserva.movendo.operacao : null;
    const semCaixa = !Reserva.dados.contasDeCaixa.length;

    return `
      <article class="linha reserva${a.inativa ? ' do-ciclo' : ''}">
        <div class="linha-corpo">
          <strong>${Formato.texto(a.nome)}</strong>
          <div class="hstack gap6 wrap">
            ${a.informadoEm
              ? `<span class="tele">INFORMADO EM ${Formato.dia(a.informadoEm)}</span>
                 <span class="tele">${Reserva.idade(a.diasDeIdade)}</span>`
              : '<span class="tele">SEM VALOR INFORMADO</span>'}
            ${a.desatualizada ? '<span class="tag pend">DESATUALIZADA</span>' : ''}
            ${a.inativa ? '<span class="tag inativa">INATIVA</span>' : ''}
          </div>
          ${informando ? Reserva.formulario(a) : ''}
          ${movendo ? Reserva.formularioDeMovimento(a, movendo) : ''}
        </div>

        <div class="linha-valor pos">${Formato.dinheiro(a.saldoRealizadoCentavos)}</div>

        <div class="linha-acoes">
          ${informando || movendo ? '' : `
            <button class="btn sm ghost" type="button" data-reserva="informar"
                    data-id="${a.id}">Informar valor</button>
            <button class="btn sm ghost" type="button" data-reserva="aportar"
                    data-id="${a.id}"${semCaixa || a.inativa ? ' disabled' : ''}>Aportar</button>
            <button class="btn sm ghost" type="button" data-reserva="resgatar"
                    data-id="${a.id}"${semCaixa || a.inativa ? ' disabled' : ''}>Resgatar</button>
            ${semCaixa
              ? '<span class="tele">SEM CONTA DE CAIXA PARA MOVER DINHEIRO</span>' : ''}`}
        </div>
      </article>`;
  },

  idade(dias) {
    if (dias === 0) return 'HOJE';
    return dias === 1 ? 'HÁ 1 DIA' : `HÁ ${dias} DIAS`;
  },

  formulario(a) {
    return `
      <form class="hstack gap6 wrap mt10" id="fReserva">
        <div class="field" style="min-width:180px">
          <label for="reservaValor">Quanto vale hoje</label>
          <input id="reservaValor" type="text" inputmode="decimal" placeholder="0,00"
                 value="${(a.saldoRealizadoCentavos / 100).toLocaleString('pt-BR',
                   { minimumFractionDigits: 2, maximumFractionDigits: 2 })}" required>
        </div>
        <button class="btn sm primary" type="button" data-reserva="gravar"
                data-id="${a.id}">Gravar</button>
        <button class="btn sm ghost" type="button" data-reserva="desistir">Desistir</button>
        <p class="dica">A diferença vira um lançamento de <b>rendimento</b> nesta conta, com a
          data de hoje. Nada é sobrescrito, e ele aparece no Extrato.</p>
      </form>`;
  },

  formularioDeMovimento(a, operacao) {
    const aporte = operacao === 'APORTE';
    const rotulo = aporte ? 'De qual conta sai' : 'Para qual conta vai';

    return `
      <form class="hstack gap6 wrap mt10" id="fMovimento">
        <div class="field" style="min-width:180px">
          <label for="movimentoConta">${rotulo}</label>
          <select id="movimentoConta">
            ${Reserva.dados.contasDeCaixa.map((c) =>
              `<option value="${c.id}">${Formato.texto(c.nome)}</option>`).join('')}
          </select>
        </div>
        <div class="field" style="min-width:140px">
          <label for="movimentoValor">Quanto</label>
          <input id="movimentoValor" type="text" inputmode="decimal" placeholder="0,00" required>
        </div>
        <div class="field" style="min-width:200px">
          <label for="movimentoDescricao">Descrição</label>
          <input id="movimentoDescricao" type="text" maxlength="200"
                 value="${aporte ? 'Aporte em ' : 'Resgate de '}${Formato.texto(a.nome)}"
                 required>
        </div>
        <button class="btn sm primary" type="button" data-reserva="mover"
                data-id="${a.id}">${aporte ? 'Aportar' : 'Resgatar'}</button>
        <button class="btn sm ghost" type="button" data-reserva="desistir">Desistir</button>
        <p class="dica">${aporte
          ? 'Guardar dinheiro <b>não é gasto</b>: nascem dois lançamentos com categoria de sistema, e o <b>patrimônio não muda</b> — o dinheiro só troca de bolso.'
          : 'O resgate devolve o dinheiro ao caixa. São dois lançamentos, e o <b>patrimônio continua o mesmo</b>.'}</p>
      </form>`;
  },

  async mover(contaId) {
    const valor = Formato.centavos(document.getElementById('movimentoValor').value);
    if (valor === null || valor <= 0) {
      Reserva.avisar('O valor não dá para ler, ou é zero. Use 1234,56.');
      return;
    }
    const outra = Number(document.getElementById('movimentoConta').value);
    const aporte = Reserva.movendo.operacao === 'APORTE';

    try {
      await API.transferir(Contexto.ambiente.id, {
        contaDeOrigemId: aporte ? outra : contaId,
        contaDeDestinoId: aporte ? contaId : outra,
        valor,
        dataEvento: Formato.hoje(),
        descricao: document.getElementById('movimentoDescricao').value,
      });
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Reserva.avisar(erro.paraGente());
      return;
    }
    Reserva.movendo = null;
    await Reserva.recarregar();
  },

  async gravar(contaId) {
    const valorCentavos = Formato.centavos(document.getElementById('reservaValor').value);
    if (valorCentavos === null || valorCentavos < 0) {
      Reserva.avisar('O valor não dá para ler. Use 1234,56.');
      return;
    }
    try {
      Reserva.dados = await API.informarValorDaAplicacao(Contexto.ambiente.id, contaId,
        { valorCentavos });
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Reserva.avisar(erro.paraGente());
      return;
    }
    Reserva.informando = null;
    Reserva.desenhar();
  },

  avisar(mensagem) {
    const alvo = document.getElementById('avisoReserva');
    if (alvo) alvo.innerHTML = `<div class="aviso err">${Formato.texto(mensagem)}</div>`;
  },
};
