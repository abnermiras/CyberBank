const Formato = {
  dinheiro(centavos) {
    const valor = (Math.abs(centavos) / 100).toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
    return `${centavos < 0 ? '−' : ''}R$ ${valor}`;
  },

  dia(iso) {
    if (!iso) return '—';
    const [ano, mes, dia] = iso.split('-');
    return `${dia}/${mes}/${ano}`;
  },

  hoje() {
    const agora = new Date().toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' });
    const [dia, mes, ano] = agora.split('/');
    return `${ano}-${mes}-${dia}`;
  },

  centavos(texto) {
    const limpo = String(texto).trim().replace(/\./g, '').replace(',', '.');
    if (limpo === '' || Number.isNaN(Number(limpo))) return null;
    return Math.round(Number(limpo) * 100);
  },

  texto(bruto) {
    return String(bruto ?? '').replace(/[&<>"']/g, (c) =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
  },
};
