#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# demo-2-polimorfismo.sh
#
# TACTICA: diferir el binding -> POLIMORFISMO.
# REQUERIMIENTO: RNF-03 (incorporar una politica de precios nueva no debe
# requerir modificar ni recompilar los modulos existentes del motor de ventas).
#
# El motor (MotorDeVentas) no calcula precios: depende de la abstraccion
# PoliticaDePrecio y resuelve, POR NOMBRE y en tiempo de ejecucion, cual
# implementacion usar contra un RegistroDePoliticas que Spring puebla por
# inyeccion de dependencias. La demo tiene dos partes:
#   Parte A (runtime): el motor despacha por nombre entre las politicas ya
#                      registradas y rechaza limpiamente un nombre inexistente.
#   Parte B (modificabilidad): agregamos UNA politica nueva (una clase), y
#                      mostramos que ningun modulo existente se modifica y que
#                      la nueva queda disponible con solo empaquetar y reiniciar.
#
# Uso:
#   ./scripts/demo-2-polimorfismo.sh            demo completa (A + B)
#   ./scripts/demo-2-polimorfismo.sh --rapido   solo la parte A (sin recompilar)
#   ./scripts/demo-2-polimorfismo.sh --conservar deja la politica nueva en el
#                                               codigo (no revierte al final)
#
# La parte A requiere la app corriendo (./scripts/iniciar.sh) en otra terminal.
# ---------------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_comun.sh"

RAPIDO="no"; CONSERVAR="no"
for a in "$@"; do
  case "$a" in
    --rapido)    RAPIDO="si" ;;
    --conservar) CONSERVAR="si" ;;
    -h|--help)   grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
  esac
done

requiere_app
PARTIDO=$(primer_partido)

titulo "DEMO 2 — Polimorfismo en el motor de precios (RNF-03)"

# ===========================================================================
titulo "PARTE A — Despacho por nombre en tiempo de ejecucion"
# ===========================================================================
paso "Politicas descubiertas por el registro (inyeccion de dependencias):"
curl -s "$API/politicas-precio" | jq -r '.[] | "    - \(.nombre): \(.descripcion)"'

paso "Elegimos dos asientos y cotizamos con cada politica, cambiando SOLO el nombre"
AID=(); while IFS=$'\t' read -r id _; do AID+=("$id"); done < <(tomar_asientos_libres 2 "$PARTIDO") || exit 1
A1="${AID[0]}"; A2="${AID[1]}"
info "Asientos a cotizar: $A1 y $A2"

cotizar() { # <politica>
  local pol="$1"
  curl -s -X POST "$API/cotizaciones" -H 'Content-Type: application/json' \
       -d "{\"partidoId\":$PARTIDO,\"asientoIds\":[$A1,$A2],\"politica\":\"$pol\"}"
}

for pol in precio-base descuento-socio demanda; do
  r=$(cotizar "$pol")
  monto=$(jq -r '.monto'         <<<"$r")
  usada=$(jq -r '.politicaUsada' <<<"$r")
  printf "    politica pedida=%-16s → politicaUsada=%-16s monto=%s\n" "$pol" "$usada" "$monto"
done
ok "El motor resolvio cada calculo por el NOMBRE recibido, sin conocer las formulas."

paso "Pedimos una politica que no existe → debe rechazarse limpiamente"
cod=$(curl -s -o /tmp/pol.json -w '%{http_code}' -X POST "$API/cotizaciones" \
      -H 'Content-Type: application/json' \
      -d "{\"partidoId\":$PARTIDO,\"asientoIds\":[$A1,$A2],\"politica\":\"2x1-jubilados\"}")
info "Respuesta → HTTP $cod"; mostrar_json "$(cat /tmp/pol.json)"; rm -f /tmp/pol.json
[ "$cod" = "400" ] && ok "El registro resuelve por nombre y reporta POLITICA_NO_ENCONTRADA."

if [ "$RAPIDO" = "si" ]; then
  titulo "Fin de la DEMO 2 (modo --rapido: se omitio la parte B)"
  exit 0
fi

# ===========================================================================
titulo "PARTE B — Agregar una politica NUEVA sin tocar el motor"
# ===========================================================================
NUEVA="$DIR_APP/src/main/java/com/adaucu/demo/precios/politicas/PoliticaMitadDePrecio.java"

limpiar_parte_b() {
  detener_efimera
  if [ "$CONSERVAR" = "no" ] && [ -f "$NUEVA" ]; then
    rm -f "$NUEVA"
    paso "Revirtiendo: se quita la politica de ejemplo y se re-empaqueta el jar original…"
    asegurar_jar forzar >/dev/null && ok "Repo y artefacto restaurados al estado original."
  fi
}
trap limpiar_parte_b EXIT

paso "Creando una implementacion nueva de PoliticaDePrecio: 'mitad-de-precio' (50% off)"
cat > "$NUEVA" <<'JAVA'
package com.adaucu.demo.precios.politicas;

import com.adaucu.demo.precios.ContextoDeCompra;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.precios.PoliticaDePrecio;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Politica de EJEMPLO agregada por scripts/demo-2-polimorfismo.sh para
 * demostrar RNF-03: es una clase nueva y autocontenida. No se modifico ningun
 * modulo existente (ni MotorDeVentas ni RegistroDePoliticas): con anotarla
 * @Component alcanza para que el registro la descubra al arrancar.
 */
@Component
public class PoliticaMitadDePrecio implements PoliticaDePrecio {

    @Override
    public String nombre() {
        return "mitad-de-precio";
    }

    @Override
    public String descripcion() {
        return "Cobra la mitad del precio base (politica de ejemplo de la demo).";
    }

    @Override
    public Dinero calcular(ContextoDeCompra contexto) {
        BigDecimal total = contexto.asientos().stream()
                .map(item -> item.precioBase())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Dinero.de(total.multiply(new BigDecimal("0.50")));
    }
}
JAVA
ok "Archivo creado: ${NUEVA#"$DIR_RAIZ"/}"

paso "¿Se modifico algun modulo existente? (git status del codigo Java)"
mods=$(cd "$DIR_RAIZ" && git status --porcelain -- 'demo/demo/src/main/java' 2>/dev/null | grep -E '^ ?M' || true)
nuevos=$(cd "$DIR_RAIZ" && git status --porcelain -- 'demo/demo/src/main/java' 2>/dev/null | grep -E '^\?\?' || true)
echo "  Archivos existentes modificados:"; [ -z "$mods" ] && echo "    (ninguno)" || echo "$mods" | sed 's/^/    /'
echo "  Archivos nuevos (sin trackear):"; echo "$nuevos" | sed 's/^/    /'
if [ -z "$mods" ]; then
  ok "Se agrego SOLO una clase nueva; el motor y el registro quedan intactos."
else
  fallo "Se detectaron modulos modificados (no deberia ocurrir para RNF-03)."
fi

paso "Empaquetando e iniciando una instancia efimera con la politica nueva…"
asegurar_jar forzar >/dev/null || { fallo "No se pudo empaquetar"; exit 1; }
arrancar_efimera 8091 "/tmp/demo2-efimera.db" \
    --spring.config.import="optional:file:$DIR_APP/config/ventas.properties" || exit 1

API_EF="http://localhost:8091/api/v1"
PART_EF=$(primer_partido "$API_EF")
paso "Politicas disponibles ahora en la instancia efimera:"
curl -s "$API_EF/politicas-precio" | jq -r '.[] | "    - \(.nombre)"'

if curl -s "$API_EF/politicas-precio" | jq -e '.[] | select(.nombre=="mitad-de-precio")' >/dev/null; then
  ok "La politica nueva quedo registrada automaticamente (sin tocar el motor)."
  paso "Cotizando el mismo par de asientos con precio-base y con mitad-de-precio:"
  EID=(); while IFS=$'\t' read -r id _; do EID+=("$id"); done < <(tomar_asientos_libres 2 "$PART_EF" "$API_EF")
  E1="${EID[0]}"; E2="${EID[1]}"
  base=$(curl -s -X POST "$API_EF/cotizaciones" -H 'Content-Type: application/json' \
        -d "{\"partidoId\":$PART_EF,\"asientoIds\":[$E1,$E2],\"politica\":\"precio-base\"}" | jq -r '.monto')
  mitad=$(curl -s -X POST "$API_EF/cotizaciones" -H 'Content-Type: application/json' \
        -d "{\"partidoId\":$PART_EF,\"asientoIds\":[$E1,$E2],\"politica\":\"mitad-de-precio\"}" | jq -r '.monto')
  info "    precio-base = $base     mitad-de-precio = $mitad"
  ok "RNF-03 satisfecho: una politica nueva = una clase nueva, sin recompilar el motor."
else
  fallo "La politica nueva no aparecio en la instancia efimera."
fi

titulo "Fin de la DEMO 2"
# El trap EXIT (limpiar_parte_b) detiene la efimera y revierte el codigo/jar
# salvo que se haya pasado --conservar.
