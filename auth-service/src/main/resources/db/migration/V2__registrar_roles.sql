-- Incorpora los roles faltantes sin modificar los existentes.
-- Los identificadores los genera PostgreSQL.

INSERT INTO public.roles (nombre, descripcion)
VALUES
    ('ADMIN_EMPRESA', 'Administra su empresa y sus hostales'),
    ('RECEPCIONISTA', 'Gestiona reservas, huéspedes y pagos en sus sedes')
    ON CONFLICT (nombre) DO NOTHING;