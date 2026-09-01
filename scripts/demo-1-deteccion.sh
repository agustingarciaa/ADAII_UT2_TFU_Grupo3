#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# demo-1-deteccion.sh
#
# TACTICA: deteccion de estados desprotegidos -> TIMESTAMP.
# REQUERIMIENTOS: RNF-01 (detectar el conflicto ante compras concurrentes y
# rechazar una, notificandola) y RNF-02 (nunca dos entradas para el mismo
# asiento / no superar la capacidad).
#
# Idea de la tactica: al listar los asientos, cada uno viaja con una
# "marcaTiempo". El cliente la devuelve al reservar. El servidor solo cambia
# el asiento a RESERVADO si la marca que trae el pedido sigue coincidiendo con
# la almacenada (comparar-y-fijar). Si otro se adelanto, la marca ya cambio y
# la reserva se rechaza: asi se detecta el estado desprotegido.
#
# Requiere la app corriendo (./scripts/iniciar.sh) en otra terminal.
# ---------------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_comun.sh"
requiere_app

PARTIDO=$(primer_partido)

titulo "DEMO 1 — Deteccion de conflictos con timestamp (RNF-01 y RNF-02)"

# --- Dos usuarios distintos, como en el escenario real de RNF-01 ---
paso "Registrando dos hinchas que competiran por el mismo asiento"
TOKEN_A=$(registrar_login "Ana")   || exit 1
TOKEN_B=$(registrar_login "Bruno") || exit 1

# --- Un asiento LIBRE; ambos leen la MISMA marcaTiempo ---
paso "Eligiendo un asiento LIBRE y leyendo su marcaTiempo (lo que veria el cliente)"
IFS=$'\t' read -r ASIENTO MARCA < <(tomar_asiento_libre "$PARTIDO")
info "Asiento en disputa: id=$ASIENTO   marcaTiempo observada=$MARCA"
CUERPO="{\"partidoId\":$PARTIDO,\"asientos\":[{\"asientoId\":$ASIENTO,\"marcaTiempo\":\"$MARCA\"}]}"

# ---------------------------------------------------------------------------
titulo "RNF-01 — Dos solicitudes CONCURRENTES por el mismo asiento"
# ---------------------------------------------------------------------------
paso "Ana y Bruno envian su reserva al mismo tiempo (en paralelo)"

RESP_A="$(mktemp)"; RESP_B="$(mktemp)"; COD_A="$(mktemp)"; COD_B="$(mktemp)"
curl -s -o "$RESP_A" -w '%{http_code}' -X POST "$API/reservas" \
     -H "Authorization: Bearer $TOKEN_A" -H 'Content-Type: application/json' \
     -d "$CUERPO" > "$COD_A" &
curl -s -o "$RESP_B" -w '%{http_code}' -X POST "$API/reservas" \
     -H "Authorization: Bearer $TOKEN_B" -H 'Content-Type: application/json' \
     -d "$CUERPO" > "$COD_B" &
wait

ca=$(cat "$COD_A"); cb=$(cat "$COD_B")
echo; info "Respuesta a Ana   → HTTP $ca"; mostrar_json "$(cat "$RESP_A")"
echo;  info "Respuesta a Bruno → HTTP $cb"; mostrar_json "$(cat "$RESP_B")"
echo

# Exactamente uno acepta (201) y exactamente uno se rechaza por conflicto (409).
aceptados=0; rechazados=0
for c in "$ca" "$cb"; do
  [ "$c" = "201" ] && aceptados=$((aceptados+1))
  [ "$c" = "409" ] && rechazados=$((rechazados+1))
done

if [ "$aceptados" -eq 1 ] && [ "$rechazados" -eq 1 ]; then
  ok "RNF-01 satisfecho: se acepto UNA reserva y se rechazo la otra."
  # El cuerpo del rechazo (409) notifica cual reserva quedo rechazada y sobre
  # que asiento hubo conflicto -> "notificando aquella reserva rechazada".
  rechazo="$RESP_A"; [ "$cb" = "409" ] && rechazo="$RESP_B"
  rid=$(jq -r '.reservaId'  "$rechazo")
  cod=$(jq -r '.codigo'     "$rechazo")
  ok "Notificacion del rechazo → codigo=$cod, reservaId=$rid, asientosEnConflicto:"
  jq -c '.asientosEnConflicto[]' "$rechazo" | sed 's/^/    /'
else
  fallo "Resultado inesperado (aceptados=$aceptados, rechazados=$rechazados)"
fi
rm -f "$RESP_A" "$RESP_B" "$COD_A" "$COD_B"

# ---------------------------------------------------------------------------
titulo "RNF-02 — Nunca dos entradas para el mismo asiento"
# ---------------------------------------------------------------------------
paso "El asiento ya quedo RESERVADO; su marcaTiempo cambio. Verifiquemoslo:"
estado=$(curl -s "$API/partidos/$PARTIDO/asientos" | jq -r ".[] | select(.id==$ASIENTO) | .estado")
marca_nueva=$(curl -s "$API/partidos/$PARTIDO/asientos" | jq -r ".[] | select(.id==$ASIENTO) | .marcaTiempo")
info "Asiento $ASIENTO → estado=$estado   marcaTiempo nueva=$marca_nueva   (antes: $MARCA)"

paso "Un tercer usuario intenta el MISMO asiento con la marca vieja (estado desprotegido)"
TOKEN_C=$(registrar_login "Carla") || exit 1
cod=$(curl -s -o "$RESP_A" -w '%{http_code}' -X POST "$API/reservas" \
     -H "Authorization: Bearer $TOKEN_C" -H 'Content-Type: application/json' \
     -d "$CUERPO")
RESP_A_json="$(cat "$RESP_A" 2>/dev/null)"; rm -f "$RESP_A"
info "Respuesta → HTTP $cod"; mostrar_json "$RESP_A_json"
if [ "$cod" = "409" ]; then
  ok "RNF-02 satisfecho: el asiento ya tomado no puede reservarse otra vez,"
  ok "asi que es imposible emitir dos entradas para el mismo asiento."
  dim "Garantia adicional en la base: la tabla 'entrada' declara asiento_id UNIQUE,"
  dim "y el cambio de estado es comparar-y-fijar sobre la marcaTiempo (ver EjecutorDeReservas)."
else
  fallo "Se esperaba un conflicto 409 y se obtuvo HTTP $cod"
fi

titulo "Fin de la DEMO 1"
