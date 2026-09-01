#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# iniciar.sh - arranca la aplicacion de ticketing.
#
# Empaqueta la aplicacion (si hace falta) y la ejecuta como un .jar
# ejecutable, leyendo los parametros operativos del archivo EXTERNO
# config/ventas.properties. Ese archivo vive FUERA del artefacto: es la
# tactica de "binding en tiempo de configuracion" (RNF-04 / RNF-05); un
# operador edita esos valores y reinicia, sin recompilar ni redesplegar.
#
# Uso:
#   ./scripts/iniciar.sh              arranca (empaqueta solo si falta el jar)
#   ./scripts/iniciar.sh --rebuild    fuerza un empaquetado limpio antes
#   ./scripts/iniciar.sh --reset-db   borra la base SQLite (datos de ejemplo)
#   ./scripts/iniciar.sh --puerto 9090  usa otro puerto (por defecto 8080)
#
# Se queda en primer plano; cortar con Ctrl+C. Para las demos, dejar esto
# corriendo en una terminal y usar ./scripts/demo-*.sh en otra.
# ---------------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_comun.sh"

REBUILD="no"; RESET_DB="no"; PUERTO="8080"
while [ $# -gt 0 ]; do
  case "$1" in
    --rebuild)  REBUILD="forzar" ;;
    --reset-db) RESET_DB="si" ;;
    --puerto)   PUERTO="$2"; shift ;;
    -h|--help)  grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) fallo "Opcion desconocida: $1"; exit 1 ;;
  esac
  shift
done

# El archivo de configuracion externo (fuera del .jar) que resuelve RNF-04/05.
CONFIG_EXTERNO="$DIR_APP/config/ventas.properties"

titulo "Arranque de la aplicacion de ticketing"

asegurar_jar "$REBUILD" || exit 1

if [ "$RESET_DB" = "si" ]; then
  rm -f "$DIR_APP/ticketing.db"
  ok "Base de datos reiniciada (se volvera a sembrar con datos de ejemplo)"
fi

info "Artefacto : $(basename "$(jar_de_la_app)")"
info "Config    : $CONFIG_EXTERNO (externo al artefacto)"
info "URL       : http://localhost:$PUERTO"
dim  "Parametros operativos actuales del archivo externo:"
grep -E '^ventas\.' "$CONFIG_EXTERNO" | sed 's/^/    /'
echo

# NOTA sobre el mecanismo de carga:
#   Spring Boot solo autocarga archivos llamados application.properties/.yml
#   desde --spring.config.additional-location. Para leer un archivo con OTRO
#   nombre (ventas.properties) hay que IMPORTARLO explicitamente con
#   spring.config.import. Por eso usamos esa opcion y no additional-location.
cd "$DIR_APP" || exit 1
exec java -jar "$(jar_de_la_app)" \
    --server.port="$PUERTO" \
    --spring.config.import="optional:file:./config/ventas.properties"
