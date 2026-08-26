---
description: Validar a consistencia da documentacao e regenerar o indice
---

Rode, nesta ordem, e me mostre apenas a saída:

```bash
python3 docs/_tools/docs.py check
python3 docs/_tools/docs.py index
python3 docs/_tools/docs.py custo
```

Se houver ERRO, corrija cada um e rode `check` de novo. Não leia os documentos para
"conferir" — a ferramenta já faz isso; abra apenas os arquivos que a saída apontar.
