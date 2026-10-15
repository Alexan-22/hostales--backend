package com.hostales.reserva.persistence;

import com.hostales.reserva.bean.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;


public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEmpresaIdAndHostalIdOrderByEntradaAsc(
            Long empresaId, Long hostalId
    );

    Optional<Reserva> findByReservaIdAndEmpresaIdAndHostalId(
            Long reservaId, Long empresaId, Long hostalId
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reserva r
        WHERE r.empresaId = :empresaId
          AND r.hostalId = :hostalId
          AND r.habitacionId = :habitacionId
          AND r.estado IN ('CONFIRMADA', 'EN_CURSO', 'FINALIZADA')
          AND r.entrada < :salida
          AND (
              CASE
                  WHEN r.estado = 'FINALIZADA'
                       AND r.salidaReal IS NOT NULL
                       AND r.salidaReal < r.salida
                  THEN r.salidaReal
                  ELSE r.salida
              END
          ) > :entrada
        """)
    boolean existeCruce(
            @Param("empresaId") Long empresaId,
            @Param("hostalId") Long hostalId,
            @Param("habitacionId") Long habitacionId,
            @Param("entrada") OffsetDateTime entrada,
            @Param("salida") OffsetDateTime salida
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Reserva r
        SET r.estado = 'CANCELADA'
        WHERE r.reservaId = :reservaId
          AND r.empresaId = :empresaId
          AND r.hostalId = :hostalId
          AND r.estado = 'CONFIRMADA'
        """)
    int cancelarConfirmada(
            @Param("reservaId") Long reservaId,
            @Param("empresaId") Long empresaId,
            @Param("hostalId") Long hostalId
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Reserva r
        SET r.estado = :estadoNuevo,
            r.entradaReal = CASE
                WHEN :estadoNuevo = 'EN_CURSO' THEN :ahora
                ELSE r.entradaReal
            END,
            r.salidaReal = CASE
                WHEN :estadoNuevo = 'FINALIZADA' THEN :ahora
                ELSE r.salidaReal
            END
        WHERE r.reservaId = :reservaId
          AND r.empresaId = :empresaId
          AND r.hostalId = :hostalId
          AND r.estado = :estadoActual
        """)
    int cambiarEstado(
            @Param("reservaId") Long reservaId,
            @Param("empresaId") Long empresaId,
            @Param("hostalId") Long hostalId,
            @Param("estadoActual") String estadoActual,
            @Param("estadoNuevo") String estadoNuevo,
            @Param("ahora") OffsetDateTime ahora
    );
    @Query("""
        SELECT DISTINCT r.habitacionId
        FROM Reserva r
        WHERE r.empresaId = :empresaId
          AND r.hostalId = :hostalId
          AND r.estado IN ('CONFIRMADA', 'EN_CURSO', 'FINALIZADA')
          AND r.entrada < :salida
          AND (
              CASE
                  WHEN r.estado = 'FINALIZADA'
                       AND r.salidaReal IS NOT NULL
                       AND r.salidaReal < r.salida
                  THEN r.salidaReal
                  ELSE r.salida
              END
          ) > :entrada
        """)
    List<Long> buscarHabitacionesConCruce(
            @Param("empresaId") Long empresaId,
            @Param("hostalId") Long hostalId,
            @Param("entrada") OffsetDateTime entrada,
            @Param("salida") OffsetDateTime salida
    );

}
