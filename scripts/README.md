# Scripts de inicio y demostración de tácticas

Scripts para **iniciar la aplicación** de ticketing y **demostrar las tres
tácticas** elegidas frente a los requerimientos no funcionales.

| Táctica | Categoría | Requerimientos |
|---|---|---|
| **Timestamp** | Detección de estados desprotegidos (protección) | RNF-01, RNF-02 |
| **Polimorfismo** | Diferir el binding (facilidad de modificación) | RNF-03 |
| **Binding en tiempo de configuración** | Diferir el binding (facilidad de modificación) | RNF-04, RNF-05 |

## Requisitos

- **Java 21+** y el wrapper de Maven (`mvnw`, ya incluido).
- **curl** y **jq** (en macOS: `brew install jq`).
- Los scripts usan `bash` (probados con el bash 3.2 de macOS).

## Cómo usarlo

En **una terminal**, arrancá la aplicación (queda en primer plano):

```bash
./scripts/iniciar.sh
```

La primera vez empaqueta el `.jar` (puede tardar). Queda escuchando en
`http://localhost:8080`. Opciones:

- `./scripts/iniciar.sh --rebuild` – fuerza un empaquetado limpio.
- `./scripts/iniciar.sh --reset-db` – borra la base SQLite y vuelve a sembrar
  los datos de ejemplo (un estadio, un partido y sus 45 asientos).
- `./scripts/iniciar.sh --puerto 9090` – usa otro puerto.

En **otra terminal**, corré las demos (con la app arriba):

```bash
./scripts/demo-1-deteccion.sh      # RNF-01 / RNF-02  (timestamp)
./scripts/demo-2-polimorfismo.sh   # RNF-03           (polimorfismo)
./scripts/demo-3-configuracion.sh  # RNF-04 / RNF-05  (binding en configuración)
./scripts/demo-todos.sh            # las tres, en orden
```

## Qué demuestra cada script

### `demo-1-deteccion.sh` — Timestamp (RNF-01, RNF-02)
Al listar los asientos, cada uno viaja con una `marcaTiempo`. El cliente la
devuelve al reservar y el servidor sólo cambia el asiento a `RESERVADO` si esa
marca **sigue coincidiendo** (comparar-y-fijar).

- **RNF-01**: dos usuarios distintos envían **en paralelo** una reserva por el
  **mismo asiento**. Exactamente una se acepta (`201`) y la otra se rechaza
  (`409`), con el cuerpo notificando `reservaId` y `asientosEnConflicto`.
- **RNF-02**: un tercer usuario reintenta ese asiento con la marca vieja y es
  rechazado, de modo que nunca se emiten dos entradas para el mismo asiento.

### `demo-2-polimorfismo.sh` — Polimorfismo (RNF-03)
El motor de ventas no calcula precios: depende de la abstracción
`PoliticaDePrecio` y resuelve la implementación **por nombre en tiempo de
ejecución** contra un registro poblado por inyección de dependencias.

- **Parte A** (contra la app corriendo): lista las políticas registradas,
  cotiza cambiando sólo el nombre de política, y muestra que un nombre
  inexistente se rechaza con `POLITICA_NO_ENCONTRADA`.
- **Parte B** (modificabilidad): agrega **una clase nueva** de política,
  muestra con `git status` que **ningún módulo existente se modificó**,
  reempaqueta y levanta una instancia efímera donde la política nueva ya está
  disponible. Al terminar revierte el cambio (dejá `--conservar` para mantenerlo).
  Usá `--rapido` para correr sólo la Parte A (no recompila).

### `demo-3-configuracion.sh` — Binding en configuración (RNF-04, RNF-05)
Los parámetros operativos no están en el código: se resuelven **al arrancar**
leyendo el archivo externo `demo/demo/config/ventas.properties`.

El script muestra los valores vigentes, "edita" el archivo (una copia con
`max-entradas` 6→2 y `max-simultaneas` 3→1) y **reinicia el mismo `.jar`** en
una instancia efímera. Luego verifica que:

- **RNF-04**: reservar 3 asientos supera el nuevo máximo (2) → `LIMITE_EXCEDIDO`.
- **RNF-05**: una segunda reserva simultánea supera el nuevo máximo (1) → `LIMITE_EXCEDIDO`.

Todo sin recompilar ni redesplegar: mismo artefacto, distinta configuración.

## Nota técnica (mecanismo de carga de la configuración)

Spring Boot sólo autocarga archivos llamados `application.properties`/`.yml`
desde `--spring.config.additional-location`. Para leer un archivo con **otro
nombre** (`ventas.properties`) hay que **importarlo explícitamente**. Por eso
los scripts arrancan con:

```
--spring.config.import=optional:file:./config/ventas.properties
```

Los comentarios de `application.properties` y `config/ventas.properties`
mencionan `--spring.config.additional-location=file:./config/`; con el archivo
llamado `ventas.properties` esa forma **no** carga el archivo. Conviene
actualizar esos comentarios a la opción `spring.config.import` de arriba (o,
alternativamente, renombrar el archivo externo a `application.properties`).

## Limpieza

Las demos crean usuarios con email único y toman asientos libres en cada
corrida, así que pueden repetirse sin reiniciar. Para volver al estado inicial:

```bash
./scripts/iniciar.sh --reset-db
```
