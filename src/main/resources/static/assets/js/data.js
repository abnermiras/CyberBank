const CampoDeData = {
  ATALHOS: {
    hoje: 0,
    ontem: -1,
    anteontem: -2,
    amanha: 1,
    amanhã: 1,
  },

  ligar(id) {
    const texto = document.getElementById(id);
    const botao = document.getElementById(`${id}Grid`);

    botao.addEventListener('click', () => {
      Calendario.abrir(botao, CampoDeData.valor(id) || Formato.hoje(), (iso) => {
        CampoDeData.definir(id, iso);
        texto.dispatchEvent(new Event('change', { bubbles: true }));
      });
    });

    texto.addEventListener('focus', () => texto.select());
    texto.addEventListener('blur', () => {
      const iso = CampoDeData.interpretar(texto.value);
      if (iso) texto.value = Formato.dia(iso);
      texto.classList.toggle('invalido', Boolean(texto.value.trim()) && !iso);
    });
  },

  definir(id, iso) {
    document.getElementById(id).value = Formato.dia(iso);
    document.getElementById(id).classList.remove('invalido');
  },

  valor(id) {
    return CampoDeData.interpretar(document.getElementById(id).value);
  },

  interpretar(bruto) {
    const cru = String(bruto || '').trim().toLowerCase();
    if (!cru) return null;

    if (CampoDeData.ATALHOS[cru] !== undefined) {
      return CampoDeData.somarDias(Formato.hoje(), CampoDeData.ATALHOS[cru]);
    }

    const so = cru.replace(/[^0-9]/g, '');
    let dia;
    let mes;
    let ano;

    if (/^\d{4}-\d{2}-\d{2}$/.test(cru)) {
      [ano, mes, dia] = cru.split('-').map(Number);
    } else if (so.length === 8) {
      dia = Number(so.slice(0, 2));
      mes = Number(so.slice(2, 4));
      ano = Number(so.slice(4));
    } else if (so.length === 6) {
      dia = Number(so.slice(0, 2));
      mes = Number(so.slice(2, 4));
      ano = 2000 + Number(so.slice(4));
    } else if (so.length === 4) {
      dia = Number(so.slice(0, 2));
      mes = Number(so.slice(2, 4));
      ano = Number(Formato.hoje().slice(0, 4));
    } else {
      return null;
    }

    return CampoDeData.montar(dia, mes, ano);
  },

  montar(dia, mes, ano) {
    if (!dia || !mes || !ano || mes > 12 || dia > 31) return null;
    const data = new Date(Date.UTC(ano, mes - 1, dia));
    if (data.getUTCDate() !== dia || data.getUTCMonth() !== mes - 1) return null;
    return data.toISOString().slice(0, 10);
  },

  somarDias(iso, dias) {
    const data = new Date(`${iso}T12:00:00Z`);
    data.setUTCDate(data.getUTCDate() + dias);
    return data.toISOString().slice(0, 10);
  },
};
