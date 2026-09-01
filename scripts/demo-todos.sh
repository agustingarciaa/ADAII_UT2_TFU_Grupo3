#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# demo-todos.sh - corre las tres demostraciones en orden.
# Requiere la app corriendo (./scripts/iniciar.sh) en otra terminal.
#
# La demo 2 se corre en modo --rapido por defecto (no recompila). Para incluir
# la prueba completa de modificabilidad de la demo 2:
#   ./scripts/demo-todos.sh --completo
# ---------------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_comun.sh"
requiere_app

ARG_DEMO2="--rapido"
[ "${1:-}" = "--completo" ] && ARG_DEMO2=""

"$DIR_SCRIPTS/demo-1-deteccion.sh"
"$DIR_SCRIPTS/demo-2-polimorfismo.sh" $ARG_DEMO2
"$DIR_SCRIPTS/demo-3-configuracion.sh"

titulo "Las tres demostraciones terminaron"
