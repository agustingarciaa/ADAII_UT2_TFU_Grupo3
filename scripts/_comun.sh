# shellcheck shell=bash
# ---------------------------------------------------------------------------
# _comun.sh - funciones compartidas por los scripts de inicio y de demostracion
#
# No se ejecuta solo: los demas scripts lo cargan con `source`.
# Requiere: bash, curl, jq (y java/maven para arrancar la aplicacion).
# ---------------------------------------------------------------------------

set -uo pipefail

# --- Rutas del proyecto (resueltas a partir de la ubicacion de este archivo) ---
DIR_SCRIPTS="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIR_RAIZ="$(cd "$DIR_SCRIPTS/.." && pwd)"
DIR_APP="$DIR_RAIZ/demo/demo"

# --- Configuracion de red ---
# El puerto/URL de la instancia principal se puede sobreescribir con la
# variable de entorno BASE (util si se corre en otro host o puerto).
BASE="${BASE:-http://localhost:8080}"
API="$BASE/api/v1"

# --- Colores (se desactivan si la salida no es una terminal) ---
if [ -t 1 ]; then
  C_TIT=$'\033[1;36m'; C_OK=$'\033[1;32m'; C_ERR=$'\033[1;31m'
  C_INFO=$'\033[0;33m'; C_DIM=$'\033[2m'; C_FIN=$'\033[0m'
else
  C_TIT=""; C_OK=""; C_ERR=""; C_INFO=""; C_DIM=""; C_FIN=""
fi

titulo()  { echo; echo "${C_TIT}=== $* ===${C_FIN}"; }
paso()    { echo "${C_TIT}> $*${C_FIN}"; }
ok()      { echo "${C_OK}[OK] $*${C_FIN}"; }
fallo()   { echo "${C_ERR}[X] $*${C_FIN}"; }
info()    { echo "${C_INFO}$*${C_FIN}"; }
dim()     { echo "${C_DIM}$*${C_FIN}"; }

# Muestra un JSON indentado (o el texto crudo si no es JSON valido).
mostrar_json() { jq . 2>/dev/null <<<"$1" || echo "$1"; }

# ---------------------------------------------------------------------------
# Ubicacion del artefacto (.jar). Se prefiere el jar ejecutable de Spring Boot
# y se descarta el "-plain.jar" que a veces genera el plugin.
# ---------------------------------------------------------------------------
jar_de_la_app() {
  ls -1 "$DIR_APP"/target/demo-*.jar 2>/dev/null | grep -v -- '-plain.jar' | head -n1
}

# Empaqueta la aplicacion si todavia no hay jar (o si se fuerza con --rebuild).
asegurar_jar() {
  local forzar="${1:-no}"
  if [ "$forzar" = "forzar" ] || [ -z "$(jar_de_la_app)" ]; then
    paso "Empaquetando la aplicacion (mvnw package, sin tests)…"
    # Se invoca con 'sh' para no depender del bit de ejecucion de mvnw
    # (asi no se modifica el modo del archivo versionado).
    ( cd "$DIR_APP" && sh ./mvnw -q -DskipTests clean package ) \
      || { fallo "Fallo el empaquetado"; return 1; }
    ok "Artefacto generado: $(basename "$(jar_de_la_app)")"
  fi
}

# ---------------------------------------------------------------------------
# Espera hasta que una instancia responda en la URL dada (o agota el tiempo).
# Uso: esperar_disponible <url-base> [segundos]
# ---------------------------------------------------------------------------
esperar_disponible() {
  local base="$1"; local limite="${2:-45}"
  for _ in $(seq 1 "$limite"); do
    if curl -sf "$base/api/v1/partidos" >/dev/null 2>&1; then return 0; fi
    sleep 1
  done
  return 1
}

# Verifica que la instancia principal este arriba; si no, explica como arrancarla.
requiere_app() {
  if ! curl -sf "$API/partidos" >/dev/null 2>&1; then
    fallo "No hay una aplicacion respondiendo en $BASE"
    info  "Arrancala primero, en otra terminal:"
    echo  "    ./scripts/iniciar.sh"
    exit 1
  fi
}

# ---------------------------------------------------------------------------
# Instancia EFIMERA: una segunda copia de la MISMA aplicacion (mismo .jar),
# en otro puerto y con su propia base de datos, que los demos de reconfiguracion
# y de modificabilidad levantan y apagan sin tocar la instancia principal.
#
#   arrancar_efimera <puerto> <ruta-db> [args extra de Spring...]
#   detener_efimera
# ---------------------------------------------------------------------------
EFIMERA_PID=""
EFIMERA_DB=""
EFIMERA_LOG=""

arrancar_efimera() {
  local puerto="$1"; local db="$2"; shift 2
  EFIMERA_DB="$db"
  EFIMERA_LOG="$(mktemp -t efimera-XXXX.log)"
  rm -f "$db"
  java -jar "$(jar_de_la_app)" \
      --server.port="$puerto" \
      --spring.datasource.url="jdbc:sqlite:$db" \
      "$@" > "$EFIMERA_LOG" 2>&1 &
  EFIMERA_PID=$!
  if esperar_disponible "http://localhost:$puerto" 60; then
    ok "Instancia efimera lista en http://localhost:$puerto (pid $EFIMERA_PID)"
    return 0
  fi
  fallo "La instancia efimera no arranco; ultimas lineas del log:"
  tail -n 20 "$EFIMERA_LOG"
  detener_efimera
  return 1
}

detener_efimera() {
  [ -n "$EFIMERA_PID" ] && kill "$EFIMERA_PID" 2>/dev/null && wait "$EFIMERA_PID" 2>/dev/null
  [ -n "$EFIMERA_DB" ] && rm -f "$EFIMERA_DB"
  [ -n "$EFIMERA_LOG" ] && rm -f "$EFIMERA_LOG"
  EFIMERA_PID=""; EFIMERA_DB=""; EFIMERA_LOG=""
}

# ---------------------------------------------------------------------------
# Helpers de la API de negocio
# ---------------------------------------------------------------------------

# Registra un usuario con email unico y devuelve (por stdout) su token de sesion.
# Los mensajes informativos van a stderr para no contaminar el valor devuelto.
#   token=$(registrar_login "Ana" [base-url])
registrar_login() {
  local nombre="$1"; local base="${2:-$API}"
  local low; low=$(printf '%s' "$nombre" | tr '[:upper:]' '[:lower:]')
  local email="demo_${low}_$$_${RANDOM}@ticketing.test"
  local pass="password123"
  curl -s -X POST "$base/usuarios" -H 'Content-Type: application/json' \
       -d "{\"email\":\"$email\",\"contrasena\":\"$pass\",\"nombre\":\"$nombre\"}" >/dev/null
  local token
  token=$(curl -s -X POST "$base/sesiones" -H 'Content-Type: application/json' \
       -d "{\"email\":\"$email\",\"contrasena\":\"$pass\"}" | jq -r '.token // empty')
  if [ -z "$token" ]; then
    fallo "No se pudo autenticar al usuario $nombre" >&2
    return 1
  fi
  echo >&2 "  ${C_DIM}usuario $nombre → $email${C_FIN}"
  echo "$token"
}

# Devuelve N lineas "id<TAB>marcaTiempo" de asientos LIBRES distintos.
# El llamador las lee en arreglos con:
#   ids=(); marcas=()
#   while IFS=$'\t' read -r id m; do ids+=("$id"); marcas+=("$m"); done \
#       < <(tomar_asientos_libres 3 "$PARTIDO")
tomar_asientos_libres() {
  local n="$1"; local partido="$2"; local base="${3:-$API}"
  local salida
  salida=$(curl -s "$base/partidos/$partido/asientos" \
        | jq -r --argjson n "$n" \
            '[ .[] | select(.estado=="LIBRE") ][:$n][] | "\(.id)\t\(.marcaTiempo)"')
  if [ "$(printf '%s\n' "$salida" | grep -c .)" -lt "$n" ]; then
    fallo "No quedan suficientes asientos LIBRES ($n) para la demostracion" >&2
    return 1
  fi
  printf '%s\n' "$salida"
}

# Atajo para tomar UN asiento: imprime "id<TAB>marcaTiempo".
tomar_asiento_libre() {
  local partido="$1"; local base="${2:-$API}"
  tomar_asientos_libres 1 "$partido" "$base"
}

# id del primer partido sembrado.
primer_partido() {
  local base="${1:-$API}"
  curl -s "$base/partidos" | jq -r '.[0].id'
}
