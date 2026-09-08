#!/bin/bash
# O banco nasce com dois papeis (docs/07-operacao/build-e-run.md, ADR-0002):
#
#   dono  - dono do schema e das tabelas. So o Flyway conecta com ele.
#   app   - o papel da aplicacao. NAO e dono e NAO tem BYPASSRLS, senao o
#           Postgres nao aplica politica de RLS nenhuma e o ADR-0002 vira decoracao.
#
# Nenhum dos dois e superusuario: superusuario ignora RLS sempre.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -v dono="$DB_OWNER_USER" -v dono_senha="$DB_OWNER_PASSWORD" \
  -v app="$DB_APP_USER" -v app_senha="$DB_APP_PASSWORD" \
  -v banco="$POSTGRES_DB" <<'SQL'

CREATE ROLE :"dono" LOGIN PASSWORD :'dono_senha'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS INHERIT;

CREATE ROLE :"app" LOGIN PASSWORD :'app_senha'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS INHERIT;

-- Ninguem entra por padrao; cada papel recebe o que precisa, e nada mais.
REVOKE ALL ON DATABASE :"banco" FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- O dono cria as tabelas: e dele o schema.
ALTER SCHEMA public OWNER TO :"dono";
GRANT CONNECT ON DATABASE :"banco" TO :"dono";

-- A aplicacao le e escreve, mas nunca cria nem altera estrutura.
GRANT CONNECT ON DATABASE :"banco" TO :"app";
GRANT USAGE ON SCHEMA public TO :"app";

-- Tabela criada por migration ja nasce acessivel a aplicacao, sem GRANT manual
-- em cada migration.
ALTER DEFAULT PRIVILEGES FOR ROLE :"dono" IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"app";
ALTER DEFAULT PRIVILEGES FOR ROLE :"dono" IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO :"app";

SQL
