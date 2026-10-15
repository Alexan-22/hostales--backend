package com.hostales.pago.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;

@Component
public class PagoBloqueo {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquear(String clave) {

        entityManager.createNativeQuery(
                "SET LOCAL lock_timeout = '5s'"
        ).executeUpdate();

        entityManager.unwrap(Session.class)
                .doWork(conexion -> {
                    try (PreparedStatement sentencia =
                                 conexion.prepareStatement(
                                         """
                                         SELECT pg_advisory_xact_lock(
                                             hashtextextended(?, 0)
                                         )
                                         """
                                 )) {

                        sentencia.setString(1, clave);
                        sentencia.execute();
                    }
                });
    }
}
