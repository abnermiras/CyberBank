const Fatura = {
  contas: [],
  cartoes: [],
  cartaoId: null,
  emFocoId: null,
  dados: null,
  pagando: false,
  informandoLimite: false,
  ligado: false,

  async montar(cartaoEscolhido) {
    if (!Fatura.ligado) {
      Fatura.ligarOuvintes();
      Fatura.ligado = true;
    }

    let lista;
    try {
      lista = await API.listarContas(Contexto.ambiente.id);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Fatura.avisar(erro.paraGente());
      return;
    }

    Fatura.contas = lista.itens;
    Fatura.cartoes = lista.itens.filter((c) => c.tipo === 'CARTAO');

    if (!Fatura.cartoes.length) {
      Fatura.desenharSemCartao();
      return;
    }

    const pedido = Number(cartaoEscolhido);
    const trocou = pedido && pedido !== Fatura.cartaoId
      && Fatura.cartoes.some((c) => c.id === pedido);

    if (trocou) Fatura.cartaoId = pedido;
    else if (!Fatura.cartoes.some((c) => c.id === Fatura.cartaoId)) {
      Fatura.cartaoId = Fatura.cartoes[0].id;
      Fatura.emFocoId = null;
    }
    if (trocou) Fatura.emFocoId = null;

    await Fatura.recarregar();
  },

  async recarregar() {
    try {
      Fatura.dados = await API.listarFaturas(Contexto.ambiente.id, Fatura.cartaoId);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Fatura.avisar(erro.paraGente());
      return;
    }

    if (!Fatura.dados.itens.some((f) => f.id === Fatura.emFocoId)) {
      Fatura.emFocoId = Fatura.aQueImporta().id;
    }
    Fatura.desenhar();
  },

  aQueImporta() {
    return Fatura.dados.itens.find((f) => f.recebePagamento)
      || Fatura.dados.itens.find((f) => f.status === 'ABERTA')
      || Fatura.dados.itens[0];
  },

  emFoco() {
    return Fatura.dados.itens.find((f) => f.id === Fatura.emFocoId);
  },

  desenharSemCartao() {
    document.getElementById('faturaTele').textContent = 'NENHUM CONTRATO';
    document.getElementById('faturaContratos').classList.add('hidden');
    document.getElementById('cabecalhoDoCartao').innerHTML = '';
    document.getElementById('faturaEmFoco').innerHTML = `
      <div class="panel">
        <div class="emespera">
          <h4>Nenhum contrato de cartão ainda</h4>
          <p>A fatura é o recorte de um período de uma conta <b>CARTÃO</b>: sem contrato não há
            ciclo para recortar. Abra uma no <b>Cadastro → contas e meios</b>, com o dia do
            vencimento e quantos dias antes ela fecha — a primeira fatura nasce com ela, vazia.</p>
          <div class="falta">FALTA PARA ISSO EXISTIR: <b>uma conta CARTÃO</b></div>
        </div>
      </div>`;
  },

  desenhar() {
    Fatura.desenharContratos();
    document.getElementById('faturaTele').textContent =
      `${Fatura.dados.itens.length} FATURA${Fatura.dados.itens.length > 1 ? 'S' : ''} NO CICLO`;
    document.getElementById('cabecalhoDoCartao').innerHTML = Fatura.cabecalho();
    document.getElementById('faturaEmFoco').innerHTML = Fatura.painelDaFatura();
  },

  desenharContratos() {
    const alvo = document.getElementById('faturaContratos');
    alvo.classList.toggle('hidden', Fatura.cartoes.length < 2);
    if (Fatura.cartoes.length < 2) return;

    alvo.style.gridTemplateColumns = `repeat(${Fatura.cartoes.length},1fr)`;
    alvo.innerHTML = Fatura.cartoes.map((c) => `
      <button type="button" class="${c.id === Fatura.cartaoId ? 'on' : ''}"
              data-fatura="contrato" data-id="${c.id}">${Formato.texto(c.nome)}</button>`).join('');
  },

  cabecalho() {
    const c = Fatura.dados.cartao;
    const disponivel = c.limiteDisponivelCentavos;

    return `
      <div class="grid2">
        <div class="panel hot metric"><span class="corner tr"></span>
          <span class="lbl">Dívida · agora</span>
          <span class="val ${c.dividaCentavos > 0 ? 'pk' : ''}">${Formato.dinheiro(c.dividaCentavos)}</span>
          <span class="sub">O SALDO DA CONTA CARTÃO, SEM CÁLCULO PRÓPRIO</span>
        </div>
        <div class="panel metric">
          <span class="lbl">Limite disponível</span>
          <span class="val ${disponivel != null && disponivel < 0 ? 'pk' : 'cy'}">${
            disponivel == null ? '—' : Formato.dinheiro(disponivel)}</span>
          <span class="sub">${c.limiteCentavos == null
            ? 'SEM LIMITE INFORMADO'
            : `LIMITE ${Formato.dinheiro(c.limiteCentavos)} · DE ${Formato.dia(c.limiteInformadoEm)}`}</span>
        </div>
      </div>

      ${c.limitePodeEstarDesatualizado ? `<div class="aviso mt10">A dívida passou do limite que
        você informou: ele pode estar desatualizado. <b>O limite nunca trava um lançamento</b> —
        recusar uma compra que o emissor já aprovou seria o app discordando do banco.</div>` : ''}

      ${Fatura.informandoLimite ? `
        <div class="conta-edicao mt10">
          <input id="novoLimite" type="text" inputmode="decimal" placeholder="15000,00"
                 value="${c.limiteCentavos == null ? '' : (c.limiteCentavos / 100).toFixed(2).replace('.', ',')}">
          <button class="btn sm primary" type="button" data-fatura="salvar-limite">Salvar</button>
          <button class="btn sm ghost" type="button" data-fatura="cancelar">Cancelar</button>
        </div>
        <p class="dica">O sistema nunca corrige este número sozinho, e ele passa a carregar a
          data de hoje: é a idade dele que a tela mostra.</p>`
      : `<div class="conta-acoes">
          <button class="btn sm acao" type="button" data-fatura="informar-limite">Informar limite</button>
        </div>`}`;
  },

  painelDaFatura() {
    const f = Fatura.emFoco();
    const indice = Fatura.dados.itens.indexOf(f);

    return `
      <section class="panel hot mt18">
        <span class="corner tr"></span><span class="corner bl"></span>
        <div class="panel-head">
          <div class="seletor-dia">
            <button class="btn sm ghost" type="button" data-fatura="anterior"
                    aria-label="Fatura anterior"
                    ${indice >= Fatura.dados.itens.length - 1 ? 'disabled' : ''}>‹</button>
            <h3 style="margin:0;align-self:center">${Formato.mes(f.competencia)}</h3>
            <button class="btn sm ghost" type="button" data-fatura="seguinte"
                    aria-label="Fatura seguinte" ${indice <= 0 ? 'disabled' : ''}>›</button>
          </div>
          <div class="hstack gap14 wrap">
            <span class="tag">${f.status}</span>
            ${Fatura.leitura(f)}
            <span class="tele">FECHA ${Formato.dia(f.dataFechamento)} ·
              VENCE ${Formato.dia(f.dataVencimento)}</span>
          </div>
        </div>

        <div class="numeros-da-fatura mt10">
          <div class="panel metric principal">
            <span class="lbl">A pagar</span>
            <span class="val ${f.aPagarCentavos > 0 ? 'ac' : 'lm'}">${Formato.dinheiro(f.aPagarCentavos)}</span>
            <span class="sub">${f.recebePagamento ? 'ESTA É A JANELA: PAGAR OU ABRIR' : 'NADA A PAGAR AQUI'}</span>
          </div>
          <div class="panel metric">
            <span class="lbl">Total</span>
            <span class="val">${Formato.dinheiro(f.totalCentavos)}</span>
            <span class="sub">SOMA DOS LANÇAMENTOS DELA</span>
          </div>
          <div class="panel metric">
            <span class="lbl">Pago</span>
            <span class="val">${Formato.dinheiro(f.pagoCentavos)}</span>
            <span class="sub">SÓ O QUE JÁ SAIU DA CONTA</span>
          </div>
          <div class="panel metric">
            <span class="lbl">${f.roladoCentavos ? 'Rolado' : 'Agendado'}</span>
            <span class="val">${Formato.dinheiro(f.roladoCentavos || f.agendadoCentavos)}</span>
            <span class="sub">${f.roladoCentavos
              ? 'FOI PARA A FATURA SEGUINTE'
              : 'MARCADO E AINDA NÃO REALIZADO'}</span>
          </div>
        </div>

        ${Fatura.pagando ? Fatura.formularioDePagamento(f) : ''}

        <div class="conta-acoes">
          ${f.recebePagamento && !Fatura.pagando
            ? `<button class="btn sm primary" type="button" data-fatura="pagar">Pagar</button>` : ''}
          ${f.recebePagamento && Fatura.ehAUltimaFechada(f) && !Fatura.pagando
            ? `<button class="btn sm acao" type="button" data-fatura="abrir">Abrir</button>` : ''}
          ${f.status === 'ABERTA' && !Fatura.pagando
            ? `<button class="btn sm acao" type="button" data-fatura="fechar">Fechar à mão</button>` : ''}
        </div>

        ${Fatura.explicarAcoes(f)}
      </section>`;
  },

  leitura(f) {
    const marcas = [];
    if (f.rolada) marcas.push('<span class="tag">ROLADA</span>');
    if (f.pagoCentavos > 0 && f.pagoCentavos < f.totalCentavos) {
      marcas.push('<span class="tag">PARCIAL</span>');
    }
    if (f.pagoCentavos >= f.totalCentavos && f.totalCentavos > 0) {
      marcas.push('<span class="tag real">QUITADA</span>');
    }
    if (f.aPagarCentavos < 0) marcas.push('<span class="tag real">CRÉDITO NO CARTÃO</span>');
    return marcas.join(' ');
  },

  ehAUltimaFechada(f) {
    const fechadas = Fatura.dados.itens.filter((x) => x.status === 'FECHADA');
    return fechadas.length > 0 && fechadas[0].id === f.id;
  },

  explicarAcoes(f) {
    if (f.status === 'FUTURA') {
      return `<p class="dica">Ela existe só para segurar parcela de mês que ainda não chegou. O
        emissor nem a emitiu: não há o que pagar aqui.</p>`;
    }
    if (f.status === 'ABERTA') {
      return `<p class="dica">O ciclo ainda está correndo e o valor ainda vai mudar — pagar
        antes do fechamento é <b>antecipar</b>, outra mecânica. <b>Fechar à mão é
        contingência</b>: o banco fechou em dia diferente, a rotina não rodou quando devia.</p>`;
    }
    if (f.recebePagamento) {
      return `<p class="dica">É esta a janela: <b>fechada e ainda devendo</b> é o que se pode
        pagar <b>e</b> o que se pode abrir — um número, duas operações. Abrir serve para
        continuar lançando nela, e devolve a seguinte para <b>FUTURA</b> sem mexer no que ela já
        tem.</p>`;
    }
    return `<p class="dica">Encerrada: o <b>a pagar</b> dela é zero, e os lançamentos dela já
      saíram de provisionado. ${f.rolada
        ? 'O que faltava rolou para a seguinte — o total histórico não cai, e a dívida do cartão não mudou.'
        : 'O pagamento cobriu o total.'} Para corrigir o passado, edita-se o lançamento no
      Extrato: <b>fatura fechada não congela nada</b>.</p>`;
  },

  formularioDePagamento(f) {
    const pagadoras = Fatura.contas.filter((c) => c.entraEmCaixa && !c.inativa);
    const padrao = Fatura.dados.cartao.contaPagadoraPadraoId;

    if (!pagadoras.length) {
      return `<div class="aviso err mt10">Nenhuma conta de caixa ativa para pagar a partir dela.
        Pagar a fatura é uma <b>transferência</b>, e ela precisa de uma origem.</div>`;
    }

    return `
      <div class="formraiz mt10">
        <div class="field" style="margin:0">
          <label for="pagaCom">Pagar com</label>
          <select id="pagaCom">${pagadoras.map((c) =>
            `<option value="${c.id}"${c.id === padrao ? ' selected' : ''}>
              ${Formato.texto(c.nome)}</option>`).join('')}</select>
        </div>
        <div class="field" style="margin:0">
          <label for="pagaValor">Valor</label>
          <input id="pagaValor" type="text" inputmode="decimal"
                 value="${(f.aPagarCentavos / 100).toFixed(2).replace('.', ',')}">
        </div>
        <div class="field" style="margin:0">
          <label for="pagaDia">Dia</label>
          <div class="campo-data">
            <input id="pagaDia" type="text" inputmode="numeric" placeholder="dd/mm/aaaa"
                   maxlength="10" value="${Formato.dia(Formato.hoje())}">
            <button class="grid-data" type="button" id="pagaDiaGrid"
                    aria-label="Escolher no calendário" title="Escolher no calendário">▦</button>
          </div>
        </div>
        <button class="btn primary" type="button" data-fatura="confirmar-pagamento">Registrar</button>
        <button class="btn ghost" type="button" data-fatura="cancelar">Cancelar</button>
      </div>
      <p class="dica" id="explicaPagamento"></p>`;
  },

  explicarPagamento() {
    const alvo = document.getElementById('explicaPagamento');
    if (!alvo) return;

    const f = Fatura.emFoco();
    const valor = Formato.centavos(document.getElementById('pagaValor').value) || 0;
    const dia = CampoDeData.valor('pagaDia');
    const conta = Fatura.contas.find(
      (c) => c.id === Number(document.getElementById('pagaCom').value));
    const futuro = dia && dia > Formato.hoje();
    const resto = f.aPagarCentavos - valor;

    alvo.innerHTML = `Vai criar <b>dois lançamentos</b> — saída de
      ${Formato.texto(conta ? conta.nome : '—')} e entrada em
      ${Formato.texto(Fatura.dados.cartao.nome)} —, com categoria de sistema e fora do relatório
      de gasto: o gasto foi contado uma vez, na compra.
      ${futuro
        ? `Marcado para <b>${Formato.dia(dia)}</b>, ele nasce <b>PREVISTO</b> e realiza pela data.`
        : 'Nasce <b>REALIZADO</b>.'}
      ${resto > 0
        ? `Sobram <b>${Formato.dinheiro(resto)}</b>: a fatura fica <b>parcial</b> e
           <b>nada é liquidado</b> — se ela vencer assim, o que sobrou rola para a seguinte.`
        : resto < 0
          ? `São <b>${Formato.dinheiro(-resto)}</b> a mais que o devido: vira <b>crédito</b> na
             conta do cartão, e isso existe na vida real.`
          : 'Quita a fatura, e é a quitação que liquida os lançamentos dela.'}`;
  },

  ligarOuvintes() {
    const tela = document.querySelector('[data-tela="fatura"]');

    tela.addEventListener('input', () => {
      if (Fatura.pagando) Fatura.explicarPagamento();
    });

    tela.addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-fatura]');
      if (!botao) return;

      const acoes = {
        contrato: () => Fatura.montar(botao.dataset.id),
        anterior: () => Fatura.andar(1),
        seguinte: () => Fatura.andar(-1),
        pagar: () => Fatura.abrirPagamento(),
        cancelar: () => Fatura.fechar(),
        'confirmar-pagamento': () => Fatura.pagar(),
        fechar: () => Fatura.tentar(
          () => API.fecharFatura(Contexto.ambiente.id, Fatura.emFocoId)),
        abrir: () => Fatura.tentar(
          () => API.abrirFatura(Contexto.ambiente.id, Fatura.emFocoId)),
        'informar-limite': () => Fatura.abrirLimite(),
        'salvar-limite': () => Fatura.salvarLimite(),
      };
      await acoes[botao.dataset.fatura]();
    });
  },

  andar(passos) {
    const indice = Fatura.dados.itens.indexOf(Fatura.emFoco()) + passos;
    if (indice < 0 || indice >= Fatura.dados.itens.length) return;

    Fatura.emFocoId = Fatura.dados.itens[indice].id;
    Fatura.pagando = false;
    Fatura.desenhar();
  },

  abrirPagamento() {
    Fatura.pagando = true;
    Fatura.informandoLimite = false;
    Fatura.desenhar();
    CampoDeData.ligar('pagaDia');
    Fatura.explicarPagamento();
    document.getElementById('pagaValor').focus();
  },

  abrirLimite() {
    Fatura.informandoLimite = true;
    Fatura.pagando = false;
    Fatura.desenhar();
    document.getElementById('novoLimite').focus();
  },

  fechar() {
    Fatura.pagando = false;
    Fatura.informandoLimite = false;
    Fatura.desenhar();
  },

  async pagar() {
    const dia = CampoDeData.valor('pagaDia');
    if (!dia) {
      Fatura.avisar('Dia inválido. Use dd/mm/aaaa, ou o calendário.');
      return;
    }

    await Fatura.tentar(() => API.pagarFatura(Contexto.ambiente.id, Fatura.emFocoId, {
      contaPagadoraId: Number(document.getElementById('pagaCom').value),
      valor: Formato.centavos(document.getElementById('pagaValor').value),
      dataEvento: dia,
    }));
  },

  async salvarLimite() {
    const limite = Formato.centavos(document.getElementById('novoLimite').value);
    await Fatura.tentar(() =>
      API.informarLimite(Contexto.ambiente.id, Fatura.cartaoId, limite));
  },

  async tentar(acao) {
    try {
      await acao();
      Fatura.avisar(null);
      Fatura.pagando = false;
      Fatura.informandoLimite = false;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Fatura.avisar(erro.paraGente());
    }
    await Fatura.recarregar();
  },

  avisar(mensagem) {
    document.getElementById('avisoFatura').innerHTML = mensagem
      ? `<div class="aviso err">${Formato.texto(mensagem)}</div>` : '';
  },
};
