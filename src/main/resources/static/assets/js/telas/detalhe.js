const Detalhe = {
  SITUACAO: { PREVISTO: 'prev', PROVISIONADO: 'prov', REALIZADO: 'real' },

  SITUACOES_QUE_O_USUARIO_ESCOLHE: ['PREVISTO', 'REALIZADO'],

  id: null,
  dados: null,
  historico: [],
  confirmando: false,
  editando: false,
  ligado: false,

  aberto() {
    return !document.getElementById('modalDetalhe').hidden;
  },

  async abrir(id) {
    Detalhe.ligar();
    Detalhe.id = id;
    Detalhe.confirmando = false;
    Detalhe.editando = false;
    document.getElementById('modalDetalhe').hidden = false;
    document.getElementById('detalheCorpo').innerHTML =
      '<span class="tele">CARREGANDO…</span>';

    let historico;
    try {
      [Detalhe.dados, historico] = await Promise.all([
        API.verLancamento(Contexto.ambiente.id, id),
        API.historicoDoAlvo(Contexto.ambiente.id, 'LANCAMENTO', id),
      ]);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Detalhe.dados = null;
      document.getElementById('detalheCorpo').innerHTML = `
        <div class="vazio">${Formato.texto(erro.paraGente())}<br>
          <b>Este lançamento não está mais aqui.</b></div>`;
      return;
    }

    Detalhe.historico = historico.itens;
    Detalhe.pintar();
  },

  esconder() {
    document.getElementById('modalDetalhe').hidden = true;
    Detalhe.id = null;
    Detalhe.dados = null;
    Detalhe.historico = [];
    Detalhe.editando = false;
  },

  fechar() {
    Detalhe.esconder();
    if ((window.location.hash || '').startsWith('#/extrato/')) {
      window.location.hash = '#/extrato';
    }
  },

  ligar() {
    if (Detalhe.ligado) return;
    Detalhe.ligado = true;

    document.getElementById('btnFecharDetalhe')
      .addEventListener('click', Detalhe.fechar);

    document.getElementById('modalDetalhe').addEventListener('click', (evento) => {
      if (evento.target.id === 'modalDetalhe') Detalhe.fechar();
    });

    document.addEventListener('keydown', (evento) => {
      if (evento.key === 'Escape' && Detalhe.aberto()) Detalhe.fechar();
    });

    document.getElementById('detalheCorpo').addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-detalhe]');
      if (!botao) return;
      const acoes = {
        confirmar: () => { Detalhe.confirmando = true; Detalhe.pintar(); },
        cancelar: () => { Detalhe.confirmando = false; Detalhe.pintar(); },
        editar: () => {
          Detalhe.editando = true;
          Detalhe.confirmando = false;
          Detalhe.pintar();
        },
        desistir: () => { Detalhe.editando = false; Detalhe.pintar(); },
        aplicar: Detalhe.aplicar,
        excluir: Detalhe.excluir,
        estornar: Detalhe.estornar,
        ir: () => Detalhe.abrir(Number(botao.dataset.id)),
      };
      await acoes[botao.dataset.detalhe]();
    });

    document.getElementById('detalheCorpo').addEventListener('change', async (evento) => {
      const seletor = evento.target.closest('[data-categoria-de]');
      if (!seletor || !seletor.value) return;
      seletor.disabled = true;
      await Detalhe.categorizar(Number(seletor.value));
    });
  },

  pintar() {
    const d = Detalhe.dados;
    const saida = d.sentido === 'SAIDA';

    document.getElementById('detalheTitulo').textContent = `Lançamento #${d.id}`;
    document.getElementById('detalheCorpo').innerHTML = `
      <div id="avisoDetalhe"></div>

      <div class="det-topo">
        <div>
          <div class="det-desc">${Formato.texto(d.descricao)}</div>
          <div class="hstack gap6 wrap mt10">
            <span class="tag ${Detalhe.SITUACAO[d.situacao]}">${d.situacao}</span>
            ${d.transferencia ? '<span class="tag transf">TRANSFERÊNCIA</span>' : ''}
            ${d.estornoDeId ? '<span class="tag transf">ESTORNO</span>' : ''}
            ${d.estornadoPorId ? '<span class="tag inativa">ESTORNADO</span>' : ''}
            ${d.doCiclo ? '<span class="tag">DO SISTEMA</span>' : ''}
          </div>
        </div>
        <div class="det-valor ${saida ? 'neg' : 'pos'}">
          ${saida ? '−' : '+'}${Formato.dinheiro(d.valor).replace('−', '')}
        </div>
      </div>

      ${Detalhe.editando
        ? Detalhe.formulario()
        : Detalhe.bloco('O dinheiro', Detalhe.dinheiro())
          + Detalhe.bloco('As duas datas', Detalhe.datas())
          + Detalhe.bloco('Classificação', Detalhe.classificacao())}
      ${Detalhe.bloco('Quem e quando', Detalhe.quemEQuando())}
      ${Detalhe.ligacoes()}
      ${Detalhe.serieEFatura()}
      ${Detalhe.oQueJaAconteceu()}
      ${Detalhe.acoes()}`;

    if (Detalhe.editando) Detalhe.ligarFormulario();
  },

  bloco(titulo, linhas) {
    return `<section class="det-bloco"><h4>${titulo}</h4><dl>${linhas}</dl></section>`;
  },

  par(rotulo, valor) {
    return `<dt>${rotulo}</dt><dd>${valor}</dd>`;
  },

  dinheiro() {
    const d = Detalhe.dados;
    const meio = Detalhe.rotuloDoMeio();
    return Detalhe.par('Conta', d.conta
        ? `${Formato.texto(d.conta.nome)} <i class="det-fraco">${d.conta.tipo}</i>`
        : '<i class="det-fraco">de outro ambiente</i>')
      + Detalhe.par('Meio', meio
        ? Formato.texto(meio)
        : '<i class="det-fraco">nenhum — ninguém pagou nada aqui</i>')
      + Detalhe.par('Sentido', d.sentido === 'SAIDA' ? 'SAÍDA' : 'ENTRADA')
      + Detalhe.par('Valor', Formato.dinheiro(d.valor).replace('−', ''));
  },

  rotuloDoMeio() {
    const d = Detalhe.dados;
    if (!d.meio) return null;
    if (d.meio.nome) return `${d.conta ? d.conta.nome : '—'} · ${d.meio.nome}`;
    const tipo = Contas.ROTULO_DE_MEIO[d.meio.tipo] || d.meio.tipo;
    return `${d.conta ? d.conta.nome : '—'} · ${tipo}`;
  },

  datas() {
    const d = Detalhe.dados;
    const iguais = d.dataEvento === d.dataEfeito;
    return Detalhe.par('Aconteceu em', Formato.dia(d.dataEvento))
      + Detalhe.par('Mexe no saldo em', Formato.dia(d.dataEfeito))
      + (iguais ? '' : `<dt></dt><dd class="det-explica">As duas datas são diferentes porque o
          meio separa o dia da compra do dia do pagamento. É por
          <b>${Formato.dia(d.dataEfeito)}</b> que o saldo se calcula; por
          <b>${Formato.dia(d.dataEvento)}</b> é que você procura.</dd>`);
  },

  classificacao() {
    const d = Detalhe.dados;
    if (!d.categoria) {
      return Detalhe.par('Categoria', `
        <div class="det-pendente">
          <span class="tag pend">SEM CATEGORIA</span>
          <select class="filtro" data-categoria-de="${d.id}">
            ${Detalhe.opcoes(d.sentido)}
          </select>
        </div>`);
    }
    const raiz = d.categoria.raiz;
    const caminho = raiz.id === d.categoria.id
      ? Formato.texto(raiz.nome)
      : `${Formato.texto(raiz.nome)} › ${Formato.texto(d.categoria.nome)}`;

    return Detalhe.par('Categoria',
      `<span class="det-cat" style="--k:${Formato.tom(raiz.cor)}">${caminho}</span>`);
  },

  opcoes(sentido) {
    const raizes = (Extrato.arvore || [])
      .filter((raiz) => !raiz.sistema && raiz.sentido === sentido && !raiz.inativa);

    if (!raizes.length) return '<option value="">— nenhuma categoria deste sentido —</option>';

    return '<option value="">— escolher —</option>'
      + raizes.filter((raiz) => raiz.escolhivel)
        .map((raiz) => `<option value="${raiz.id}">${Formato.texto(raiz.nome)}</option>`).join('')
      + raizes.filter((raiz) => !raiz.escolhivel).map((raiz) => {
        const filhas = raiz.filhas.filter((filha) => filha.escolhivel);
        if (!filhas.length) return '';
        return `<optgroup label="${Formato.texto(raiz.nome)}">`
          + filhas.map((filha) =>
            `<option value="${filha.id}">${Formato.texto(filha.nome)}</option>`).join('')
          + '</optgroup>';
      }).join('');
  },

  formulario() {
    const d = Detalhe.dados;
    if (d.doCiclo) return Detalhe.formularioDoCiclo();
    const transferencia = Boolean(d.transferencia);

    return `
      <section class="det-bloco">
        <h4>Corrigindo o registro</h4>
        ${transferencia ? `<p class="dica">Este é um lado de uma transferência: valor, data,
          descrição e situação valem para <b>os dois</b>. Conta, meio e categoria não se
          corrigem de um lado só — isso quebraria a soma zero do par.</p>` : ''}

        <form id="fCorrecao" autocomplete="off">
          <div class="row">
            <div class="field"><label for="edValor">Valor</label>
              <input id="edValor" type="text" inputmode="decimal" placeholder="0,00"
                     value="${Detalhe.emReais(d.valor)}" required></div>
            <div class="field"><label for="edData">Data do evento</label>
              <div class="campo-data">
                <input id="edData" type="text" inputmode="numeric" placeholder="dd/mm/aaaa"
                       maxlength="10" value="${Formato.dia(d.dataEvento)}" required>
                <button class="grid-data" type="button" id="edDataGrid"
                        aria-label="Escolher no calendário"
                        title="Escolher no calendário">▦</button>
              </div></div>
          </div>

          <div class="field"><label for="edDescricao">Descrição</label>
            <input id="edDescricao" type="text" maxlength="200"
                   value="${Formato.texto(d.descricao)}" required></div>

          ${transferencia ? '' : `
            <div class="row">
              <div class="field"><label for="edMeio">Meio — e a conta vem dele</label>
                <select id="edMeio">${Detalhe.opcoesDeMeio()}</select></div>
              <div class="field"><label for="edSentido">Sentido</label>
                <select id="edSentido">
                  <option value="SAIDA"${d.sentido === 'SAIDA' ? ' selected' : ''}>SAÍDA</option>
                  <option value="ENTRADA"${d.sentido === 'ENTRADA' ? ' selected' : ''}>ENTRADA</option>
                </select></div>
            </div>

            <div class="row">
              <div class="field"><label for="edCategoria">Categoria</label>
                <select id="edCategoria"></select></div>
              <div class="field hidden" id="campoEdSubcategoria">
                <label for="edSubcategoria">Subcategoria</label>
                <select id="edSubcategoria"></select></div>
            </div>`}

          <div class="row">
            <div class="field hidden" id="campoEdVencimento">
              <label for="edVencimento">Vencimento</label>
              <div class="campo-data">
                <input id="edVencimento" type="text" inputmode="numeric" placeholder="dd/mm/aaaa"
                       maxlength="10" value="${Formato.dia(d.dataEfeito)}">
                <button class="grid-data" type="button" id="edVencimentoGrid"
                        aria-label="Escolher no calendário"
                        title="Escolher no calendário">▦</button>
              </div></div>
            <div class="field"><label for="edSituacao">Situação</label>
              <select id="edSituacao">${Detalhe.opcoesDeSituacao()}</select></div>
          </div>

          <div class="explica" id="edImpacto"></div>
        </form>
      </section>`;
  },

  formularioDoCiclo() {
    return `
      <section class="det-bloco">
        <h4>Corrigindo o saldo de abertura</h4>
        <p class="dica">Este lançamento foi o sistema que criou, e dele só o <b>valor</b> é
          seu para corrigir. Conta, data, descrição e situação descrevem a abertura da conta,
          não uma escolha sua — e a abertura não se exclui.</p>

        <form id="fCorrecao" autocomplete="off">
          <div class="field"><label for="edValor">Valor</label>
            <input id="edValor" type="text" inputmode="decimal" placeholder="0,00"
                   value="${Detalhe.emReais(Detalhe.dados.valor)}" required></div>
          <div class="explica" id="edImpacto"></div>
        </form>
      </section>`;
  },

  emReais(centavos) {
    return (centavos / 100).toLocaleString('pt-BR', {
      minimumFractionDigits: 2, maximumFractionDigits: 2,
    });
  },

  meiosEscolhiveis() {
    const atual = Detalhe.dados.meio ? Detalhe.dados.meio.id : null;
    return (Extrato.meios || []).filter((meio) => !meio.inativo || meio.id === atual);
  },

  opcoesDeMeio() {
    const atual = Detalhe.dados.meio ? Detalhe.dados.meio.id : null;
    const meios = Detalhe.meiosEscolhiveis();
    if (!meios.length) return '<option value="">— sem meio de pagamento —</option>';

    return meios.map((meio) => `<option value="${meio.id}"${meio.id === atual ? ' selected' : ''}>
        ${Formato.texto(Detalhe.rotuloDe(meio))}</option>`).join('');
  },

  rotuloDe(meio) {
    return Contas.identidadeDoMeio(meio, Extrato.contas);
  },

  opcoesDeSituacao() {
    const atual = Detalhe.dados.situacao;
    const valores = Detalhe.SITUACOES_QUE_O_USUARIO_ESCOLHE.includes(atual)
      ? Detalhe.SITUACOES_QUE_O_USUARIO_ESCOLHE
      : [...Detalhe.SITUACOES_QUE_O_USUARIO_ESCOLHE, atual];

    return valores.map((situacao) =>
      `<option value="${situacao}"${situacao === atual ? ' selected' : ''}>${situacao}</option>`)
      .join('');
  },

  ligarFormulario() {
    const formulario = document.getElementById('fCorrecao');
    formulario.addEventListener('submit', (evento) => evento.preventDefault());
    formulario.addEventListener('input', Detalhe.aoMexerNoFormulario);
    formulario.addEventListener('change', Detalhe.aoMexerNoFormulario);

    if (Detalhe.dados.doCiclo) {
      Detalhe.mostrarImpacto();
      return;
    }

    CampoDeData.ligar('edData');
    CampoDeData.ligar('edVencimento');

    if (!Detalhe.dados.transferencia) {
      Detalhe.montarCategorias();
      document.getElementById('edSentido')
        .addEventListener('change', Detalhe.montarCategorias);
      document.getElementById('edCategoria')
        .addEventListener('change', Detalhe.montarSubcategorias);
    }
    Detalhe.mostrarVencimento();
    Detalhe.mostrarImpacto();
  },

  aoMexerNoFormulario(evento) {
    if (evento.target.id === 'edMeio') Detalhe.mostrarVencimento();
    Detalhe.mostrarImpacto();
  },

  meioEscolhido() {
    const campo = document.getElementById('edMeio');
    if (!campo) return null;
    return Detalhe.meiosEscolhiveis().find((meio) => String(meio.id) === campo.value) || null;
  },

  mostrarVencimento() {
    const meio = Detalhe.meioEscolhido();
    document.getElementById('campoEdVencimento')
      .classList.toggle('hidden', !meio || !meio.separaAsDuasDatas);
  },

  montarCategorias() {
    const d = Detalhe.dados;
    const sentido = document.getElementById('edSentido').value;
    const raizAtual = d.categoria && sentido === d.sentido ? d.categoria.raiz.id : null;

    const raizes = (Extrato.arvore || []).filter((raiz) =>
      raiz.sentido === sentido
      && (raiz.id === raizAtual || (!raiz.inativa
        && (raiz.escolhivel || raiz.filhas.some((filha) => filha.escolhivel)))));

    document.getElementById('edCategoria').innerHTML =
      (d.categoria ? '' : '<option value="">— sem categoria (fica pendente) —</option>')
      + raizes.map((raiz) => `<option value="${raiz.id}"${raiz.id === raizAtual ? ' selected' : ''}>
          ${Formato.texto(raiz.nome)}</option>`).join('');

    Detalhe.montarSubcategorias();
  },

  montarSubcategorias() {
    const d = Detalhe.dados;
    const raizId = Number(document.getElementById('edCategoria').value);
    const raiz = (Extrato.arvore || []).find((umaRaiz) => umaRaiz.id === raizId);
    const atual = d.categoria ? d.categoria.id : null;
    const filhas = raiz
      ? raiz.filhas.filter((filha) => filha.escolhivel || filha.id === atual)
      : [];

    document.getElementById('campoEdSubcategoria').classList.toggle('hidden', !filhas.length);
    document.getElementById('edSubcategoria').innerHTML = filhas.map((filha) =>
      `<option value="${filha.id}"${filha.id === atual ? ' selected' : ''}>
        ${Formato.texto(filha.nome)}</option>`).join('');

    Detalhe.mostrarImpacto();
  },

  correcao() {
    const d = Detalhe.dados;
    const valor = Formato.centavos(document.getElementById('edValor').value);
    if (d.doCiclo) return { valor };

    const meio = Detalhe.meioEscolhido();
    const vencimento = document.getElementById('campoEdVencimento');

    const corpo = {
      valor,
      dataEvento: CampoDeData.valor('edData'),
      descricao: document.getElementById('edDescricao').value,
      situacao: document.getElementById('edSituacao').value,
    };

    if (d.transferencia) return corpo;

    corpo.meioId = meio ? meio.id : null;
    corpo.sentido = document.getElementById('edSentido').value;

    const raizId = Number(document.getElementById('edCategoria').value);
    const subcategoria = document.getElementById('edSubcategoria');
    const temSub = !document.getElementById('campoEdSubcategoria').classList.contains('hidden');
    corpo.categoriaId = raizId
      ? (temSub && subcategoria.value ? Number(subcategoria.value) : raizId)
      : null;

    if (!vencimento.classList.contains('hidden')) {
      corpo.dataEfeito = CampoDeData.valor('edVencimento');
    }
    return corpo;
  },

  mostrarImpacto() {
    document.getElementById('edImpacto').innerHTML = Detalhe.impacto();
  },

  impacto() {
    const d = Detalhe.dados;
    const corpo = Detalhe.correcao();

    if (corpo.valor === null || (!d.doCiclo && !corpo.dataEvento)) {
      return 'Valor ou data ainda não dão para ler — o impacto aparece quando derem.';
    }

    const antes = {
      contaId: d.conta ? d.conta.id : null,
      sentido: d.sentido, valor: d.valor, situacao: d.situacao,
    };
    const meio = d.doCiclo ? null : Detalhe.meioEscolhido();
    const depois = {
      contaId: d.transferencia || !meio ? antes.contaId : meio.contaId,
      sentido: corpo.sentido || antes.sentido,
      valor: corpo.valor,
      situacao: corpo.situacao,
    };

    const linhas = [...new Set([antes.contaId, depois.contaId])]
      .filter((id) => id !== null)
      .map((id) => Detalhe.linhaDeSaldo(id, antes, depois))
      .filter(Boolean);

    if (!linhas.length) {
      return 'Nada que mexa em saldo mudou.';
    }
    return linhas.join('<br>')
      + (d.transferencia
        ? '<br>O <b>outro lado do par</b> acompanha, em sentido oposto.' : '');
  },

  linhaDeSaldo(contaId, antes, depois) {
    const conta = (Extrato.contas || []).find((c) => c.id === contaId);
    if (!conta) return '';

    const saiu = contaId === antes.contaId ? Detalhe.efeito(antes) : 0;
    const entrou = contaId === depois.contaId ? Detalhe.efeito(depois) : 0;
    if (saiu === entrou) return '';

    const resultado = conta.saldoRealizadoCentavos - saiu + entrou;
    return `Saldo de <b>${Formato.texto(conta.nome)}</b>:
      ${Formato.dinheiro(conta.saldoRealizadoCentavos)} →
      <b>${Formato.dinheiro(resultado)}</b>`;
  },

  efeito(lado) {
    if (lado.situacao === 'PREVISTO') return 0;
    return lado.sentido === 'SAIDA' ? -lado.valor : lado.valor;
  },

  async aplicar() {
    const corpo = Detalhe.correcao();
    if (corpo.valor === null) {
      Detalhe.avisar('O valor não dá para ler. Use 1234,56.');
      return;
    }
    if (!Detalhe.dados.doCiclo && !corpo.dataEvento) {
      Detalhe.avisar('Data do evento inválida. Use dd/mm/aaaa, ou o calendário.');
      return;
    }
    if (corpo.dataEfeito === null) {
      Detalhe.avisar('Vencimento inválido. Use dd/mm/aaaa, ou o calendário.');
      return;
    }

    await Detalhe.tentar(
      () => API.editarLancamento(Contexto.ambiente.id, Detalhe.id, corpo),
      () => Detalhe.abrir(Detalhe.id));
  },

  avisar(mensagem) {
    document.getElementById('avisoDetalhe').innerHTML =
      `<div class="aviso err">${Formato.texto(mensagem)}</div>`;
  },

  quemEQuando() {
    const d = Detalhe.dados;
    return Detalhe.par('Lançado por', d.autor
        ? Formato.texto(d.autor.nome)
        : '<i class="det-fraco">usuário removido</i>')
      + Detalhe.par('Cadastrado em', Detalhe.instante(d.criadoEm))
      + (d.estabelecimento
        ? Detalhe.par('Estabelecimento',
          `${Formato.texto(d.estabelecimento)}
           <i class="det-fraco">texto bruto da captura</i>`)
        : '');
  },

  instante(bruto) {
    const quando = new Date(bruto);
    return `${quando.toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' })}
      às ${quando.toLocaleTimeString('pt-BR', {
        timeZone: 'America/Sao_Paulo', hour: '2-digit', minute: '2-digit',
      })}`;
  },

  ligacoes() {
    const d = Detalhe.dados;
    const linhas = [];

    if (d.transferencia) {
      linhas.push(Detalhe.par('O outro lado',
        `<button class="chip-mais" type="button" data-detalhe="ir"
           data-id="${d.transferencia.outroLadoId}">abrir #${d.transferencia.outroLadoId} →</button>`));
    }
    if (d.estornoDeId) {
      linhas.push(Detalhe.par('Estorna',
        `<button class="chip-mais" type="button" data-detalhe="ir"
           data-id="${d.estornoDeId}">abrir #${d.estornoDeId} →</button>`));
    }
    if (d.estornadoPorId) {
      linhas.push(Detalhe.par('Estornado por',
        `<button class="chip-mais" type="button" data-detalhe="ir"
           data-id="${d.estornadoPorId}">abrir #${d.estornadoPorId} →</button>`));
    }
    return linhas.length ? Detalhe.bloco('Ligações', linhas.join('')) : '';
  },

  ROTULO_DE_STATUS: { FUTURA: 'FUTURA', ABERTA: 'ABERTA', FECHADA: 'FECHADA' },

  serieEFatura() {
    const fatura = Detalhe.dados && Detalhe.dados.fatura;
    const serie = Detalhe.dados && Detalhe.dados.serie;

    if (!fatura) {
      return `
        <section class="det-bloco">
          <h4>Série e fatura <span class="tele">NÃO SE APLICA</span></h4>
          <div class="emespera">
            <p>Só compra no <b>crédito</b> entra em fatura, e só ela se parcela. Este lançamento
              não é de crédito: não há fatura nem série a mostrar.</p>
          </div>
        </section>`;
    }

    return `
      <section class="det-bloco">
        <h4>Série e fatura</h4>
        <dl>
          ${serie ? Detalhe.par('Parcela',
            `<b>${serie.numero} de ${serie.parcelas}</b>, da compra de
             <b>${Formato.dinheiro(serie.valorDaCompraCentavos)}</b> em
             ${Formato.dia(serie.dataDaCompra)}`) : ''}
          ${Detalhe.par('Fatura', `<b>${Formato.texto(Formato.mes(fatura.competencia))}</b>
            <span class="tag">${Detalhe.ROTULO_DE_STATUS[fatura.status] || fatura.status}</span>`)}
          ${Detalhe.par('Fecha em', Formato.dia(fatura.dataFechamento))}
          ${Detalhe.par('Vence em', Formato.dia(fatura.dataVencimento))}
        </dl>
        <p class="dica">O gasto conta no mês do <b>vencimento</b> desta fatura — é o eixo padrão
          do relatório, e é o que bate com o dinheiro que sai. A fatura é editável: este
          lançamento pode ser movido para qualquer fatura deste cartão, aberta ou não.
          ${serie ? `<br><br><b>Parcela não se edita nem se exclui sozinha</b>: ela é um pedaço
            de uma compra só, e mexer nela quebraria a soma. Editar o parcelamento altera
            <b>todas</b> as parcelas, sempre — e quem se arrepende exclui o parcelamento.` : ''}</p>
      </section>`;
  },

  oQueJaAconteceu() {
    const linhas = Detalhe.historico.map((evento) => {
      const dados = evento.dados || {};
      const frase = Diario.FRASES[evento.tipo];
      return `
        <div class="det-evento${evento.origem === 'SISTEMA' ? ' do-sistema' : ''}">
          <span class="det-quando">${Formato.dia(evento.dia)}
            <i>${Diario.hora(evento.instante)}</i></span>
          <span class="det-frase">${frase ? frase(dados) : Formato.texto(evento.tipo)}</span>
          <span class="det-autor">${evento.origem === 'SISTEMA'
            ? 'A ROTINA' : Formato.texto(evento.autor || '—')}</span>
        </div>`;
    }).join('');

    return `
      <section class="det-bloco">
        <h4>O que já aconteceu com ele</h4>
        ${linhas || '<div class="vazio">Nenhum evento registrado para este lançamento.</div>'}
      </section>`;
  },

  acoes() {
    const d = Detalhe.dados;
    if (d.doCiclo && !Detalhe.editando) {
      return `<section class="det-acoes">
          <span class="tele">ESTE LANÇAMENTO É DO SISTEMA — SÓ O VALOR SE CORRIGE,
            E ELE NÃO SE EXCLUI</span>
          <button class="btn sm" type="button" data-detalhe="editar">Corrigir o valor</button>
        </section>`;
    }
    if (Detalhe.editando) {
      return `<section class="det-acoes">
          <button class="btn sm primary" type="button" data-detalhe="aplicar">Aplicar</button>
          <button class="btn sm ghost" type="button" data-detalhe="desistir">Desistir</button>
        </section>`;
    }
    if (Detalhe.confirmando) {
      return `<section class="det-acoes">
          <span class="tele" style="color:var(--pink)">${Extrato.impacto(Detalhe.paraImpacto())}</span>
          <button class="btn sm danger" type="button" data-detalhe="excluir">Confirmar</button>
          <button class="btn sm ghost" type="button" data-detalhe="cancelar">Cancelar</button>
        </section>`;
    }
    return `<section class="det-acoes">
        <button class="btn sm" type="button" data-detalhe="editar">Editar</button>
        ${d.estornoDeId ? '' : '<button class="btn sm ghost" type="button" data-detalhe="estornar">Estornar</button>'}
        <button class="btn sm danger" type="button" data-detalhe="confirmar">Excluir</button>
      </section>`;
  },

  paraImpacto() {
    const d = Detalhe.dados;
    return {
      contaId: d.conta ? d.conta.id : null,
      situacao: d.situacao,
      sentido: d.sentido,
      valor: d.valor,
      transferenciaId: d.transferencia ? d.transferencia.id : null,
    };
  },

  async categorizar(categoriaId) {
    await Detalhe.tentar(
      () => API.editarLancamento(Contexto.ambiente.id, Detalhe.id, { categoriaId }),
      () => Detalhe.abrir(Detalhe.id));
  },

  async estornar() {
    await Detalhe.tentar(
      () => API.estornarLancamento(Contexto.ambiente.id, Detalhe.id,
        { dataEvento: Formato.hoje() }),
      () => Detalhe.abrir(Detalhe.id));
  },

  async excluir() {
    await Detalhe.tentar(
      () => API.excluirLancamento(Contexto.ambiente.id, Detalhe.id),
      () => Detalhe.fechar());
  },

  async tentar(acao, aoDarCerto) {
    try {
      await acao();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      document.getElementById('avisoDetalhe').innerHTML =
        `<div class="aviso err">${Formato.texto(erro.paraGente())}</div>`;
      return;
    }
    await Extrato.recarregarTudo();
    await aoDarCerto();
  },
};
