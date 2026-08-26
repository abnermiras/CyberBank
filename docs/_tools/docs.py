#!/usr/bin/env python3
"""Ferramenta de manutencao da documentacao do Cyberbank.

  python3 docs/_tools/docs.py index     # regenera docs/INDEX.md a partir do front-matter
  python3 docs/_tools/docs.py check     # valida referencias, front-matter e tamanho
  python3 docs/_tools/docs.py custo     # estima o custo em tokens de cada rota

Rode `check` antes de commitar mudanca em docs. Zero dependencias externas.
"""
import os, re, sys, glob

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
DOCS = os.path.join(ROOT, "docs")
CHARS_POR_TOKEN = 3.3  # portugues, aproximacao conservadora

SECOES = {
    "00-produto": "Produto — o que estamos construindo e por que",
    "01-arquitetura": "Arquitetura — como o sistema e organizado",
    "02-dominio": "Dominio — as regras de negocio",
    "03-dados": "Dados — schema e persistencia",
    "04-api": "API — contratos expostos",
    "05-integracoes": "Integracoes — bordas com o mundo externo",
    "06-interface": "Interface — o que o usuario ve",
    "07-operacao": "Operacao — build, deploy e incidentes",
    "08-fluxos": "Fluxos — roteiros de tarefa (entre por aqui)",
}


def docs_files():
    for p in sorted(glob.glob(os.path.join(DOCS, "**", "*.md"), recursive=True)):
        rel = os.path.relpath(p, DOCS).replace(os.sep, "/")
        if rel.startswith("_tools/") or rel == "INDEX.md":
            continue
        yield rel, p


def front_matter(path):
    txt = open(path, encoding="utf-8").read()
    m = re.match(r"^---\n(.*?)\n---\n", txt, re.S)
    fm = {}
    if m:
        for line in m.group(1).splitlines():
            if ":" in line:
                k, v = line.split(":", 1)
                fm[k.strip()] = v.strip().strip('"')
    return fm, txt


def load():
    out = []
    for rel, path in docs_files():
        fm, txt = front_matter(path)
        out.append({"rel": rel, "path": path, "fm": fm, "txt": txt,
                    "linhas": txt.count("\n") + 1, "chars": len(txt)})
    return out


def cmd_index(docs):
    L = ["---", "id: INDEX", "titulo: Mapa completo da documentacao",
         "dono: o indice de todos os documentos", "ler-junto: []", "status: ativo",
         "---", "",
         "# Mapa da documentacao",
         "",
         "> Gerado por `docs/_tools/docs.py index`. Nao edite a mao.",
         "",
         "Leia este arquivo **so quando a tabela de roteamento do `CLAUDE.md` nao resolver**.",
         "Regras de escrita: `docs/CONVENTIONS.md`.", ""]
    avulsos = [d for d in docs if "/" not in d["rel"]]
    if avulsos:
        L += ["## Raiz", "", "| Documento | Dono do fato | Status |", "|---|---|---|"]
        for d in avulsos:
            L.append(f"| [`{d['rel']}`]({d['rel']}) | {d['fm'].get('dono','—')} | {d['fm'].get('status','—')} |")
        L.append("")
    for pasta, titulo in SECOES.items():
        grupo = [d for d in docs if d["rel"].startswith(pasta + "/")]
        if not grupo:
            continue
        L += [f"## {titulo}", "", "| Documento | Dono do fato | Status |", "|---|---|---|"]
        for d in grupo:
            nome = d["rel"][len(pasta) + 1:]
            L.append(f"| [`{nome}`]({d['rel']}) | {d['fm'].get('dono','—')} | {d['fm'].get('status','—')} |")
        L.append("")
    stubs = [d for d in docs if d["fm"].get("status") == "stub"]
    L += [f"---", "", f"**{len(docs)} documentos · {len(stubs)} ainda em stub.**",
          "Stub = conteudo inexistente: pergunte, nao deduza.", ""]
    open(os.path.join(DOCS, "INDEX.md"), "w", encoding="utf-8").write("\n".join(L))
    print(f"INDEX.md regenerado: {len(docs)} documentos, {len(stubs)} stubs")


def cmd_check(docs):
    ids = {d["fm"].get("id"): d["rel"] for d in docs}
    erros, avisos = [], []
    for d in docs:
        fm, rel = d["fm"], d["rel"]
        esperado = rel[:-3]
        for campo in ("id", "titulo", "dono", "status"):
            if campo not in fm:
                erros.append(f"{rel}: front-matter sem '{campo}'")
        if fm.get("id") and fm["id"] != esperado:
            erros.append(f"{rel}: id '{fm['id']}' != caminho '{esperado}'")
        for ref in re.findall(r"[\w/\-]+", fm.get("ler-junto", "").strip("[]")):
            if ref and "*" not in ref and ref not in ids:
                erros.append(f"{rel}: ler-junto aponta para id inexistente '{ref}'")
        for ref in set(re.findall(r"docs/[\w\-/]+\.md", d["txt"])):
            if not os.path.exists(os.path.join(ROOT, ref)):
                erros.append(f"{rel}: link quebrado -> {ref}")
        if fm.get("status") == "ativo" and d["linhas"] > 300:
            avisos.append(f"{rel}: {d['linhas']} linhas — passou de 300, considere quebrar")
    claude = os.path.join(ROOT, "CLAUDE.md")
    if os.path.exists(claude):
        txt = open(claude, encoding="utf-8").read()
        for ref in set(re.findall(r"docs/[\w\-/]+\.md", txt)):
            if not os.path.exists(os.path.join(ROOT, ref)):
                erros.append(f"CLAUDE.md: link quebrado -> {ref}")
        n = txt.count("\n") + 1
        if n > 100:
            avisos.append(f"CLAUDE.md: {n} linhas — e lido em TODA sessao, mantenha < 100")
    for e in erros:
        print("ERRO   " + e)
    for a in avisos:
        print("AVISO  " + a)
    print(f"\n{len(docs)} documentos · {len(erros)} erros · {len(avisos)} avisos")
    return 1 if erros else 0


def cmd_custo(docs):
    por_rel = {d["rel"]: d for d in docs}
    base = 0
    claude = os.path.join(ROOT, "CLAUDE.md")
    if os.path.exists(claude):
        base = len(open(claude, encoding="utf-8").read())
    print(f"Contexto base (CLAUDE.md, lido sempre): ~{int(base/CHARS_POR_TOKEN)} tokens\n")
    print("Custo por rota (base + docs obrigatorios do fluxo):\n")
    for d in sorted(docs, key=lambda x: x["rel"]):
        if not d["rel"].startswith("08-fluxos/"):
            continue
        bloco = d["txt"].split("**Condicionais")[0].split("**Nao abra")[0]
        refs = [r[len("docs/"):] for r in re.findall(r"docs/[\w\-/]+\.md", bloco)]
        total = base + d["chars"] + sum(por_rel[r]["chars"] for r in refs if r in por_rel)
        faltando = [r for r in refs if r not in por_rel]
        nota = f"  (!! {len(faltando)} nao encontrado)" if faltando else ""
        print(f"  {d['rel'][10:-3]:<28} ~{int(total/CHARS_POR_TOKEN):>6} tokens  "
              f"({1+len(refs)} docs){nota}")
    total_tudo = sum(d["chars"] for d in docs) + base
    print(f"\nLer TUDO custaria ~{int(total_tudo/CHARS_POR_TOKEN)} tokens — "
          f"e o que voce evita a cada interacao.")
    print("Obs.: stubs sao pequenos hoje; refaca esta conta quando estiverem escritos.")


if __name__ == "__main__":
    cmd = sys.argv[1] if len(sys.argv) > 1 else "check"
    d = load()
    sys.exit({"index": cmd_index, "check": cmd_check, "custo": cmd_custo}[cmd](d) or 0)
