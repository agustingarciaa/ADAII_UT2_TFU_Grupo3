#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# demo-3-configuracion.sh
#
# TACTICA: diferir el binding -> BINDING EN TIEMPO DE CONFIGURACION.
# REQUERIMIENTOS: RNF-04 (reconfigurar el maximo de entradas por compra) y
# RNF-05 (reconfigurar el maximo de compras simultaneas por usuario), en ambos
# casos SIN conocimiento tecnico y SIN redesplegar.
#
# Los parametros del motor no estan fijados en el codigo: se resuelven al
# ARRANCAR, leyendo el archivo externo config/ventas.properties. Cambiar un
# valor es editar el archivo y reiniciar; el mismo artefacto (.jar) pasa a
# comportarse distinto. Esta demo lo prueba levantando una instancia efimera
# del MISMO jar con una copia editada del archivo, sin recompilar nada.
#
# Requiere la app corriendo (./scripts/iniciar.sh) en otra terminal.
# ---------------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_comun.sh"
requiere_app

titulo "DEMO 3 — Binding en tiempo de configuracion (RNF-04 y RNF-05)"

paso "Parametros que la instancia PRINCIPAL resolvio al arrancar (solo lectura):"
curl -s "$API/configuracion" | jq .
dim "Estos valores salen del archivo externo, no de constantes del codigo."

# --- Preparamos una copia EDITADA del archivo de configuracion ---
# (esto es lo que haria un operador: abrir el archivo, cambiar numeros, guardar)
paso "Un operador edita config/ventas.properties: max-entradas 6→2 y max-simultaneas 3→1"
TMPCFG="$(mktemp -d)"
sed -e 's/^ventas.max-entradas-por-compra=.*/ventas.max-entradas-por-compra=2/' \
    -e 's/^ventas.max-compras-simultaneas-por-usuario=.*/ventas.max-compras-simultaneas-por-usuario=1/' \
    "$DIR_APP/config/ventas.properties" > "$TMPCFG/ventas.properties"
info "Archivo editado:"; grep -E '^ventas\.(max-entradas|max-compras)' "$TMPCFG/ventas.properties" | sed 's/^/    /'

trap 'detener_efimera; rm -rf "$TMPCFG"' EXIT

paso "Reiniciando el servicio con ese archivo (mismo .jar, sin recompilar)…"
asegurar_jar >/dev/null || exit 1
arrancar_efimera 8092 "/tmp/demo3-efimera.db" \
    --spring.config.import="optional:file:$TMPCFG/ventas.properties" || exit 1

API_EF="http://localhost:8092/api/v1"
PART_EF=$(primer_partido "$API_EF")

paso "Parametros que la instancia REINICIADA resolvio al arrancar:"
curl -s "$API_EF/configuracion" | jq .
if [ "$(curl -s "$API_EF/configuracion" | jq -r '.maxEntradasPorCompra')" = "2" ]; then
  ok "El cambio quedo activo tras el reinicio, sin recompilar ni redesplegar."
fi

# ---------------------------------------------------------------------------
titulo "RNF-04 — Nuevo limite de entradas por compra (ahora 2)"
# ---------------------------------------------------------------------------
TOKEN=$(registrar_login "Dora" "$API_EF") || exit 1
paso "Intentando reservar 3 asientos (supera el nuevo maximo de 2)"
SID=(); SMK=()
while IFS=$'\t' read -r id m; do SID+=("$id"); SMK+=("$m"); done \
    < <(tomar_asientos_libres 3 "$PART_EF" "$API_EF") || exit 1
S1="${SID[0]}"; M1="${SMK[0]}"; S2="${SID[1]}"; M2="${SMK[1]}"; S3="${SID[2]}"; M3="${SMK[2]}"
cod=$(curl -s -o /tmp/d4.json -w '%{http_code}' -X POST "$API_EF/reservas" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"partidoId\":$PART_EF,\"asientos\":[
        {\"asientoId\":$S1,\"marcaTiempo\":\"$M1\"},
        {\"asientoId\":$S2,\"marcaTiempo\":\"$M2\"},
        {\"asientoId\":$S3,\"marcaTiempo\":\"$M3\"}]}")
info "Respuesta → HTTP $cod"; mostrar_json "$(cat /tmp/d4.json)"; rm -f /tmp/d4.json
[ "$cod" = "422" ] && ok "RNF-04 satisfecho: el limite reconfigurado (2) se aplica de inmediato." \
                    || fallo "Se esperaba 422 LIMITE_EXCEDIDO y llego HTTP $cod"

# ---------------------------------------------------------------------------
titulo "RNF-05 — Nuevo limite de compras simultaneas por usuario (ahora 1)"
# ---------------------------------------------------------------------------
paso "Primera reserva (1 asiento) del usuario: debe aceptarse"
cod=$(curl -s -o /tmp/d5a.json -w '%{http_code}' -X POST "$API_EF/reservas" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"partidoId\":$PART_EF,\"asientos\":[{\"asientoId\":$S1,\"marcaTiempo\":\"$M1\"}]}")
info "Respuesta → HTTP $cod ($(jq -r '.estado // .codigo' /tmp/d5a.json 2>/dev/null))"; rm -f /tmp/d5a.json
[ "$cod" = "201" ] && ok "Aceptada: el usuario ya tiene 1 reserva activa (el maximo)."

paso "Segunda reserva simultanea (otro asiento): debe rechazarse por el limite"
cod=$(curl -s -o /tmp/d5b.json -w '%{http_code}' -X POST "$API_EF/reservas" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"partidoId\":$PART_EF,\"asientos\":[{\"asientoId\":$S2,\"marcaTiempo\":\"$M2\"}]}")
info "Respuesta → HTTP $cod"; mostrar_json "$(cat /tmp/d5b.json)"; rm -f /tmp/d5b.json
[ "$cod" = "422" ] && ok "RNF-05 satisfecho: el limite de compras simultaneas (1) se aplica de inmediato." \
                    || fallo "Se esperaba 422 LIMITE_EXCEDIDO y llego HTTP $cod"

titulo "Fin de la DEMO 3"
dim "La instancia principal (puerto 8080) siguio con sus valores originales todo el tiempo:"
dim "el cambio vivio solo en el archivo externo de la instancia reiniciada."
