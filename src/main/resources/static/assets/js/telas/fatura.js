const Fatura = {
  ROTULO_DE_STATUS: { FUTURA: 'FUTURA', ABERTA: 'ABERTA', FECHADA: 'FECHADA' },

  cartoes: [],
  contas: [],
  cartaoId: null,
  dados: null,
  pagando: null,
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
    if (pedido && Fatura.cartoes.some((c) => c.id === pedido)) {
      Fatura.cartaoId = pedido;
    } else if (!Fatura.cartoes.some((c) => c.id === Fatura.cartaoId)) {
      Fatura.cartaoId = Fatura.cartoes[0].id;
    }

    Fatura.montarSeletor();
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

    Fatura.desenhar();
  },

  montarSeletor() {
    const seletor = document.getElementById('faturaCartao');
    seletor.innerHTML = Fatura.cartoes
      .map((c) => `<option value="${c.id}"${c.id === Fatura.cartaoId ? ' selected' : ''}>
        ${Formato.texto(c.nome)}</option>`).join('');
    seletor.classList.toggle('hidden', Fatura.cartoes.length < 2);
  },

  desenharSemCartao() {
    document.getElementById('faturaCartao').classList.add('hidden');
    document.getElementById('cabecalhoDoCartao').innerHTML = '';
    document.getElementById('listaDeFaturas').innerHTML = `
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
    document.getElementById('cabecalhoDoCartao').innerHTML = Fatura.cabecalho();
    document.getElementById('listaDeFaturas').innerHTML = Fatura.dados.itens
      .map((fatura) => Fatura.cartao(fatura)).join('');
  },

  cabecalho() {
    const c = Fatura.dados.cartao;
    const disponivel = c.limiteDisponivelCentavos;

    return `
      <section class="panel hot">
        <span class="corner tr"></span><span class="corner bl"></span>
        <div class="panel-head">
          <h3>${Formato.texto(c.nome)}</h3>
          <span class="tele">FECHA ${c.diasAntesFechamento} DIAS ANTES · VENCE DIA ${c.diaVencimento}</span>
        </div>

        <div class="kpis">
          <div class="kpi"><span class="rot">Dívida · agora</span>
            <b class="${c.dividaCentavos > 0 ? 'neg' : ''}">${Formato.dinheiro(c.dividaCentavos)}</b>
            <span class="sub">O SALDO DA CONTA CARTÃO, SEM CÁLCULO PRÓPRIO</span></div>
          <div class="kpi"><span class="rot">Limite disponível</span>
            <b class="${disponivel != null && disponivel < 0 ? 'neg' : ''}">${
              disponivel == null ? '—' : Formato.dinheiro(disponivel)}</b>
            <span class="sub">${c.limiteCentavos == null
              ? 'SEM LIMITE INFORMADO'
              : `LIMITE ${Formato.dinheiro(c.limiteCentavos)} · INFORMADO EM ${Formato.dia(c.limiteInformadoEm)}`}</span></div>
        </div>

        ${c.limitePodeEstarDesatualizado ? `<div class="aviso">A dívida passou do limite que você
          informou: ele pode estar desatualizado. <b>O limite nunca trava um lançamento</b> —
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
            <button class="btn sm ghost" type="button" data-fatura="informar-limite">Informar limite</button>
          </div>`}
      </section>`;
  },

  cartao(fatura) {
    const pagando = Fatura.pagando === fatura.id;

    return `
      <section class="panel mt18${fatura.status === 'ABERTA' ? ' hot' : ''}" id="fatura${fatura.id}">
        <div class="panel-head">
          <h3>${Formato.mes(fatura.competencia)}</h3>
          <div class="hstack gap14 wrap">
            <span class="tag">${Fatura.ROTULO_DE_STATUS[fatura.status]}</span>
            ${Fatura.leitura(fatura)}
            <span class="tele">FECHA ${Formato.dia(fatura.dataFechamento)} ·
              VENCE ${Formato.dia(fatura.dataVencimento)}</span>
          </div>
        </div>

        <dl class="det-lista">
          <dt>Total</dt><dd>${Formato.dinheiro(fatura.totalCentavos)}</dd>
          <dt>Pago</dt><dd>${Formato.dinheiro(fatura.pagoCentavos)}</dd>
          ${fatura.roladoCentavos
            ? `<dt>Rolado</dt><dd>${Formato.dinheiro(fatura.roladoCentavos)}</dd>` : ''}
          ${fatura.agendadoCentavos
            ? `<dt>Agendado</dt><dd>${Formato.dinheiro(fatura.agendadoCentavos)}
               <i class="det-fraco">ainda não saiu, e por isso não entra no pago</i></dd>` : ''}
          <dt>A pagar</dt><dd><b>${Formato.dinheiro(fatura.aPagarCentavos)}</b></dd>
        </dl>

        ${pagando ? Fatura.formularioDePagamento(fatura) : ''}

        <div class="conta-acoes">
          ${fatura.recebePagamento && !pagando
            ? `<button class="btn sm entra" type="button" data-fatura="pagar" data-id="${fatura.id}">Pagar</button>` : ''}
          ${fatura.recebePagamento && Fatura.ehAUltimaFechada(fatura) && !pagando
            ? `<button class="btn sm ghost" type="button" data-fatura="abrir" data-id="${fatura.id}">Abrir</button>` : ''}
          ${fatura.status === 'ABERTA' && !pagando
            ? `<button class="btn sm ghost" type="button" data-fatura="fechar" data-id="${fatura.id}">Fechar à mão</button>` : ''}
        </div>

        ${Fatura.explicarAcoes(fatura)}
      </section>`;
  },

  leitura(fatura) {
    const marcas = [];
    if (fatura.rolada) marcas.push('<span class="tag">ROLADA</span>');
    if (fatura.pagoCentavos > 0 && fatura.pagoCentavos < fatura.totalCentavos) {
      marcas.push('<span class="tag">PARCIAL</span>');
    }
    if (fatura.pagoCentavos >= fatura.totalCentavos && fatura.totalCentavos > 0) {
      marcas.push('<span class="tag real">QUITADA</span>');
    }
    if (fatura.aPagarCentavos < 0) marcas.push('<span class="tag real">CRÉDITO NO CARTÃO</span>');
    return marcas.join(' ');
  },

  ehAUltimaFechada(fatura) {
    const fechadas = Fatura.dados.itens.filter((f) => f.status === 'FECHADA');
    return fechadas.length > 0 && fechadas[0].id === fatura.id;
  },

  explicarAcoes(fatura) {
    if (fatura.status === 'FUTURA') {
      return `<p class="dica">Ela existe só para segurar parcela de mês que ainda não chegou. O
        emissor nem a emitiu: não há o que pagar aqui.</p>`;
    }
    if (fatura.status === 'ABERTA') {
      return `<p class="dica">O ciclo ainda está correndo e o valor ainda vai mudar — pagar
        antes do fechamento é <b>antecipar</b>, outra mecânica. <b>Fechar à mão é
        contingência</b>: o banco fechou em dia diferente, a rotina não rodou quando devia.</p>`;
    }
    if (fatura.recebePagamento) {
      return `<p class="dica">É esta a janela: <b>fechada e ainda devendo</b> é o que se pode
        pagar <b>e</b> o que se pode abrir — um número, duas operações. Abrir serve para
        continuar lançando nela, e devolve a seguinte para <b>FUTURA</b> sem mexer no que ela já
        tem.</p>`;
    }
    return `<p class="dica">Encerrada: o <b>a pagar</b> dela é zero, e os lançamentos dela já
      saíram de provisionado. ${fatura.rolada
        ? 'O que faltava rolou para a seguinte — o total histórico não cai, e a dívida do cartão não mudou.'
        : 'O pagamento cobriu o total.'} Para corrigir o passado, edita-se o lançamento no
      Extrato: <b>fatura fechada não congela nada</b>.</p>`;
  },

  formularioDePagamento(fatura) {
    const pagadoras = Fatura.contas.filter((c) => c.entraEmCaixa && !c.inativa);
    const padrao = Fatura.dados.cartao.contaPagadoraPadraoId;

    if (!pagadoras.length) {
      return `<div class="aviso err">Nenhuma conta de caixa ativa para pagar a partir dela.
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
                 value="${(fatura.aPagarCentavos / 100).toFixed(2).replace('.', ',')}">
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
        <button class="btn primary" type="button" data-fatura="confirmar-pagamento"
                data-id="${fatura.id}">Registrar</button>
        <button class="btn ghost" type="button" data-fatura="cancelar">Cancelar</button>
      </div>
      <p class="dica" id="explicaPagamento"></p>`;
  },

  explicarPagamento(fatura) {
    const alvo = document.getElementById('explicaPagamento');
    if (!alvo) return;

    const valor = Formato.centavos(document.getElementById('pagaValor').value) || 0;
    const dia = CampoDeData.valor('pagaDia');
    const conta = Fatura.contas.find((c) => c.id === Number(document.getElementById('pagaCom').value));
    const futuro = dia && dia > Formato.hoje();
    const resto = fatura.aPagarCentavos - valor;

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
    document.getElementById('faturaCartao').addEventListener('change', async (evento) => {
      Fatura.cartaoId = Number(evento.target.value);
      Fatura.pagando = null;
      await Fatura.recarregar();
    });

    document.querySelector('[data-tela="fatura"]').addEventListener('input', () => {
      if (Fatura.pagando) Fatura.explicarPagamento(Fatura.emFatura(Fatura.pagando));
    });

    document.querySelector('[data-tela="fatura"]').addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-fatura]');
      if (!botao) return;
      const id = Number(botao.dataset.id);

      const acoes = {
        pagar: () => Fatura.abrirPagamento(id),
        cancelar: () => Fatura.fechar(),
        'confirmar-pagamento': () => Fatura.pagar(id),
        fechar: () => Fatura.tentar(() => API.fecharFatura(Contexto.ambiente.id, id)),
        abrir: () => Fatura.tentar(() => API.abrirFatura(Contexto.ambiente.id, id)),
        'informar-limite': () => Fatura.abrirLimite(),
        'salvar-limite': () => Fatura.salvarLimite(),
      };
      await acoes[botao.dataset.fatura]();
    });
  },

  emFatura(id) {
    return Fatura.dados.itens.find((f) => f.id === id);
  },

  abrirPagamento(id) {
    Fatura.pagando = id;
    Fatura.informandoLimite = false;
    Fatura.desenhar();
    CampoDeData.ligar('pagaDia');
    Fatura.explicarPagamento(Fatura.emFatura(id));
    document.getElementById('pagaValor').focus();
  },

  abrirLimite() {
    Fatura.informandoLimite = true;
    Fatura.pagando = null;
    Fatura.desenhar();
    document.getElementById('novoLimite').focus();
  },

  fechar() {
    Fatura.pagando = null;
    Fatura.informandoLimite = false;
    Fatura.desenhar();
  },

  async pagar(id) {
    const dia = CampoDeData.valor('pagaDia');
    if (!dia) {
      Fatura.avisar('Dia inválido. Use dd/mm/aaaa, ou o calendário.');
      return;
    }

    await Fatura.tentar(() => API.pagarFatura(Contexto.ambiente.id, id, {
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
      Fatura.pagando = null;
      Fatura.informandoLimite = false;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Fatura.avisar(erro.paraGente());
    }
    await Fatura.montar();
  },

  avisar(mensagem) {
    document.getElementById('avisoFatura').innerHTML = mensagem
      ? `<div class="aviso err">${Formato.texto(mensagem)}</div>` : '';
  },
};
