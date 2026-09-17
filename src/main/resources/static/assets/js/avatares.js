const Avatares = {
  DESENHOS: {
    VISOR: `<path d="M10 26a14 14 0 0 1 28 0v4a8 8 0 0 1-8 8H18a8 8 0 0 1-8-8z"/>
            <rect x="14" y="21" width="20" height="8" rx="2"/>
            <path d="M18 25h12"/>`,

    OLHO: `<circle cx="24" cy="24" r="15"/><circle cx="24" cy="24" r="6"/>
           <path d="M32.5 24h5M28.3 31.4l2.5 4.3M19.7 31.4l-2.5 4.3
                    M15.5 24h-5M19.7 16.6l-2.5-4.3M28.3 16.6l2.5-4.3"/>`,

    GATO: `<path d="M13 24c0-6 5-11 11-11s11 5 11 11v4c0 6-5 11-11 11s-11-5-11-11z"/>
           <path d="M14 23l-2-9 9 4"/><path d="M34 23l1-6"/><path d="M35 17v-4"/>
           <circle cx="35" cy="10" r="2.5"/>
           <path d="M19 26h.01M29 26h.01" stroke-width="4"/><path d="M22 32h4"/>`,

    CAVEIRA: `<path d="M12 23a12 12 0 0 1 24 0v6l-3 4H15l-3-4z"/>
              <path d="M18 33v5h12v-5"/><path d="M22 33v5M26 33v5"/>
              <rect x="17" y="21" width="6" height="6"/><rect x="25" y="21" width="6" height="6"/>`,

    DRONE: `<rect x="18" y="18" width="12" height="12" rx="2"/>
            <path d="M18 18l-4-4M30 18l4-4M18 30l-4 4M30 30l4 4"/>
            <circle cx="11" cy="11" r="5"/><circle cx="37" cy="11" r="5"/>
            <circle cx="11" cy="37" r="5"/><circle cx="37" cy="37" r="5"/>`,

    CIRCUITO: `<rect x="19" y="19" width="10" height="10"/>
               <path d="M24 19v-9M24 29v9M19 24h-9M29 24h9"/>
               <circle cx="24" cy="8" r="2.5"/><circle cx="24" cy="40" r="2.5"/>
               <circle cx="8" cy="24" r="2.5"/><circle cx="40" cy="24" r="2.5"/>
               <path d="M29 21h5v-6M19 27h-5v6"/>`,

    ONDA: `<path d="M5 24h4l3-10 4 20 4-24 4 22 4-13 3 7h6"/>
           <path d="M5 38h38" opacity=".45"/>`,

    TORRE: `<path d="M16 41L24 11l8 30"/><path d="M20 28h8M17.5 35h13"/>
            <circle cx="24" cy="9" r="2.5"/>
            <path d="M31 9a9 9 0 0 1 3 7M17 9a9 9 0 0 0-3 7"/>`,

    PRISMA: `<path d="M23 11l13 25H10z"/><path d="M3 25h11"/>
             <path d="M33 22l10-5M35.5 27h8M37 32l7 5"/>`,

    ROBO: `<rect x="12" y="16" width="24" height="21" rx="3"/>
           <path d="M24 16v-5"/><circle cx="24" cy="8" r="2.5"/>
           <circle cx="19" cy="25" r="2.5"/><circle cx="29" cy="25" r="2.5"/>
           <path d="M19 32h10"/><path d="M12 23H9v6h3M36 23h3v6h-3"/>`,
  },

  svg(nome) {
    const desenho = Avatares.DESENHOS[nome];
    if (!desenho) return '';
    return `<svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="2.2"
                 stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${desenho}</svg>`;
  },
};
