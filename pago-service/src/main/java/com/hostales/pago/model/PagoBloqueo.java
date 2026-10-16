package com.hostales.pago.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class PagoBloqueo {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquear(String clave) {
        entityManager.unwrap(Session.class).doWork(conexion -> {
            try {
                try (Statement configuracion = conexion.createStatement()) {
                    configuracion.execute("SET LOCAL lock_timeout = '5s'");
                }

                try (PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))"
                )) {
                    sentencia.setString(1, clave);
                    sentencia.execute();
                }
            } catch (SQLException ex) {
                String estado = ex.getSQLState();

                if ("55P03".equals(estado)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Otra operación mantiene ocupado el pago. Reintenta la solicitud",
                            ex
                    );
                }

                if ("40P01".equals(estado) || "40001".equals(estado)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "La operación tuvo un conflicto concurrente. Reintenta la solicitud",
                            ex
                    );
                }

                if (estado != null && estado.startsWith("08")) {
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "No se pudo conectar con la base de datos de pagos",
                            ex
                    );
                }

                throw ex;
            }
        });
    }
}
