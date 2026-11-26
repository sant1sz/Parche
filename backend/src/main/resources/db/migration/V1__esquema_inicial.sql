-- Fase 0: esquema base de Parche.
-- Montos en pesos colombianos como enteros (BIGINT), sin decimales.
-- Toda tabla que pertenece a un negocio lleva id_negocio.

CREATE TABLE negocio (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    slug            VARCHAR(60)  NOT NULL,
    barrio          VARCHAR(80),
    ciudad          VARCHAR(80)  NOT NULL DEFAULT 'Medellín',
    es_demo         BOOLEAN      NOT NULL DEFAULT FALSE,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_negocio_slug UNIQUE (slug),
    CONSTRAINT ck_negocio_slug CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

COMMENT ON COLUMN negocio.es_demo IS 'TRUE si el negocio y sus datos son simulados (demostración).';

CREATE TABLE usuario (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_negocio       BIGINT       NOT NULL REFERENCES negocio (id),
    nombre           VARCHAR(120) NOT NULL,
    correo           VARCHAR(254) NOT NULL,
    hash_contrasena  VARCHAR(100) NOT NULL,
    rol              VARCHAR(20)  NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('DUENO', 'MESERO', 'COCINA'))
);

-- El personal inicia sesión con su correo, que es único sin importar mayúsculas.
CREATE UNIQUE INDEX uq_usuario_correo ON usuario (lower(correo));
CREATE INDEX ix_usuario_negocio ON usuario (id_negocio);

CREATE TABLE mesa (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_negocio      BIGINT       NOT NULL REFERENCES negocio (id),
    numero          INTEGER      NOT NULL,
    token           VARCHAR(64)  NOT NULL,
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_mesa_token UNIQUE (token),
    CONSTRAINT uq_mesa_negocio_numero UNIQUE (id_negocio, numero),
    CONSTRAINT ck_mesa_numero CHECK (numero > 0),
    CONSTRAINT ck_mesa_token CHECK (token ~ '^[A-Za-z0-9_-]{16,64}$')
);

COMMENT ON COLUMN mesa.token IS 'Identificador opaco que va en el QR o la etiqueta NFC de la mesa.';

CREATE TABLE categoria (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_negocio      BIGINT       NOT NULL REFERENCES negocio (id),
    nombre          VARCHAR(80)  NOT NULL,
    orden           INTEGER      NOT NULL DEFAULT 0,
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_categoria_negocio_nombre UNIQUE (id_negocio, nombre),
    -- Permite que producto exija que su categoría sea del mismo negocio.
    CONSTRAINT uq_categoria_id_negocio UNIQUE (id, id_negocio)
);

CREATE TABLE producto (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_negocio      BIGINT       NOT NULL REFERENCES negocio (id),
    id_categoria    BIGINT       NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    descripcion     VARCHAR(300),
    precio          BIGINT       NOT NULL,
    disponible      BOOLEAN      NOT NULL DEFAULT TRUE,
    orden           INTEGER      NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_producto_categoria_mismo_negocio
        FOREIGN KEY (id_categoria, id_negocio) REFERENCES categoria (id, id_negocio),
    CONSTRAINT uq_producto_negocio_nombre UNIQUE (id_negocio, nombre),
    CONSTRAINT ck_producto_precio CHECK (precio >= 0)
);

CREATE INDEX ix_producto_categoria ON producto (id_categoria);
CREATE INDEX ix_producto_negocio_disponible ON producto (id_negocio) WHERE disponible;
