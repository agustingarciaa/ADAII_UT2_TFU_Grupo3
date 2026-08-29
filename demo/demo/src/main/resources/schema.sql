-- Esquema autorado a mano en vez de dejarselo a ddl-auto: la generacion
-- automatica de hibernate-community-dialects para SQLite no emite
-- "integer primary key autoincrement" para las columnas identity de algunas
-- entidades (deja el tipo vacio) ni la restriccion UNIQUE compuesta de
-- asiento, asi que las tablas se declaran aca con la sintaxis correcta de
-- SQLite. El mapeo de columnas sigue siendo el que generan las anotaciones
-- JPA (nombres via @Column/@JoinColumn); Hibernate solo hace INSERT/SELECT
-- sobre estas tablas, nunca las crea.

create table if not exists estadio (
    id integer primary key autoincrement,
    capacidad integer not null,
    nombre varchar(255)
);

create table if not exists partido (
    id integer primary key autoincrement,
    comienza_en timestamp,
    estadio_id integer,
    equipo_local varchar(255),
    equipo_visitante varchar(255),
    estado varchar(255) check (estado in ('PROGRAMADO','EN_JUEGO','FINALIZADO','CANCELADO'))
);

create table if not exists asiento (
    id integer primary key autoincrement,
    numero integer not null,
    precio_base numeric(38,2),
    marca_tiempo timestamp not null,
    partido_id integer,
    estado varchar(255) check (estado in ('LIBRE','RESERVADO','VENDIDO')),
    fila varchar(255),
    sector varchar(255),
    unique (partido_id, sector, fila, numero)
);

create table if not exists usuario (
    id integer primary key autoincrement,
    email varchar(255) not null unique,
    hash_contrasena varchar(255) not null,
    nombre varchar(255),
    rol varchar(255) check (rol in ('HINCHA','SOCIO','ADMINISTRADOR')),
    salt varchar(255) not null
);

create table if not exists sesion (
    id integer primary key autoincrement,
    creada_en timestamp,
    expira_en timestamp,
    usuario_id integer,
    token varchar(255) not null unique
);

create table if not exists reserva (
    id integer primary key autoincrement,
    monto_total numeric(38,2),
    creada_en timestamp,
    expira_en timestamp,
    partido_id integer,
    usuario_id integer,
    estado varchar(255) check (estado in ('PENDIENTE','CONFIRMADA','RECHAZADA','EXPIRADA','CANCELADA')),
    politica_aplicada varchar(255)
);

create table if not exists reserva_asiento (
    id integer primary key autoincrement,
    asiento_id integer,
    reserva_id integer
);

create table if not exists compra (
    id integer primary key autoincrement,
    monto_total numeric(38,2),
    pagada_en timestamp,
    reserva_id integer,
    usuario_id integer
);

create table if not exists entrada (
    id integer primary key autoincrement,
    asiento_id integer unique,
    compra_id integer,
    emitida_en timestamp,
    partido_id integer,
    codigo varchar(255) not null unique
);
