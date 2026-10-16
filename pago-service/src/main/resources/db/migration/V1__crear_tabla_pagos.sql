-- Estructura inicial de pagos.
-- Se ejecuta en bases nuevas.
-- La base existente se incorpora mediante baseline en versión 1.

CREATE TABLE public.pagos (
                              pago_id BIGINT GENERATED ALWAYS AS IDENTITY,
                              empresa_id BIGINT NOT NULL,
                              hostal_id BIGINT NOT NULL,
                              reserva_id BIGINT NOT NULL,

                              monto NUMERIC(12, 2) NOT NULL,
                              moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
                              metodo VARCHAR(20) NOT NULL,
                              referencia VARCHAR(100),

                              estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO',
                              observaciones VARCHAR(500),

                              pagado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              clave_operacion UUID NOT NULL,
                              anulado_en TIMESTAMPTZ,
                              motivo_anulacion VARCHAR(500),

                              CONSTRAINT pagos_pkey
                                  PRIMARY KEY (pago_id),

                              CONSTRAINT uq_pago_empresa_clave_operacion
                                  UNIQUE (empresa_id, clave_operacion),

                              CONSTRAINT chk_pago_monto
                                  CHECK (monto > 0),

                              CONSTRAINT chk_pago_moneda
                                  CHECK (moneda ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_pago_metodo
        CHECK (
            metodo IN (
                'EFECTIVO',
                'YAPE',
                'PLIN',
                'TARJETA',
                'TRANSFERENCIA'
            )
        ),

    CONSTRAINT chk_pago_estado
        CHECK (estado IN ('REGISTRADO', 'ANULADO')),

    CONSTRAINT chk_pago_anulacion
        CHECK (
            (
                estado = 'REGISTRADO'
                AND anulado_en IS NULL
                AND motivo_anulacion IS NULL
            )
            OR
            (
                estado = 'ANULADO'
                AND anulado_en IS NOT NULL
                AND motivo_anulacion IS NOT NULL
                AND LENGTH(TRIM(motivo_anulacion)) > 0
            )
        )
);

CREATE INDEX idx_pagos_reserva
    ON public.pagos (empresa_id, hostal_id, reserva_id);

CREATE INDEX idx_pagos_reserva_registrados
    ON public.pagos (empresa_id, hostal_id, reserva_id)
    WHERE estado = 'REGISTRADO';