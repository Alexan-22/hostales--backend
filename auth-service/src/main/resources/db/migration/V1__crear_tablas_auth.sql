-- Estructura inicial de autenticación.
-- En bases existentes se registra mediante baseline versión 1.

CREATE TABLE public.roles (
                              rol_id BIGINT GENERATED ALWAYS AS IDENTITY,
                              nombre VARCHAR(40) NOT NULL,
                              descripcion VARCHAR(200),

                              CONSTRAINT roles_pkey PRIMARY KEY (rol_id),
                              CONSTRAINT roles_nombre_key UNIQUE (nombre)
);

CREATE TABLE public.usuarios (
                                 usuario_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                 empresa_id BIGINT NOT NULL,
                                 rol_id BIGINT NOT NULL,

                                 nombre VARCHAR(100) NOT NULL,
                                 apellido VARCHAR(100) NOT NULL,
                                 correo VARCHAR(150) NOT NULL,
                                 password_hash VARCHAR(255) NOT NULL,

                                 estado BOOLEAN NOT NULL DEFAULT TRUE,
                                 creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT usuarios_pkey
                                     PRIMARY KEY (usuario_id),

                                 CONSTRAINT uq_usuario_correo
                                     UNIQUE (correo),

                                 CONSTRAINT uq_usuario_empresa
                                     UNIQUE (empresa_id, usuario_id),

                                 CONSTRAINT usuarios_rol_id_fkey
                                     FOREIGN KEY (rol_id)
                                         REFERENCES public.roles (rol_id),

                                 CONSTRAINT chk_usuario_nombre
                                     CHECK (LENGTH(TRIM(nombre)) > 0),

                                 CONSTRAINT chk_usuario_apellido
                                     CHECK (LENGTH(TRIM(apellido)) > 0),

                                 CONSTRAINT chk_usuario_correo
                                     CHECK (
                                         correo = LOWER(TRIM(correo))
                                             AND correo <> ''
                                         ),

                                 CONSTRAINT chk_usuario_password
                                     CHECK (LENGTH(TRIM(password_hash)) > 0)
);

CREATE TABLE public.usuarios_hostales (
                                          empresa_id BIGINT NOT NULL,
                                          usuario_id BIGINT NOT NULL,
                                          hostal_id BIGINT NOT NULL,

                                          CONSTRAINT usuarios_hostales_pkey
                                              PRIMARY KEY (empresa_id, usuario_id, hostal_id),

                                          CONSTRAINT fk_acceso_usuario
                                              FOREIGN KEY (empresa_id, usuario_id)
                                                  REFERENCES public.usuarios (empresa_id, usuario_id)
);