package com.hostales.reserva.persistence;

import com.hostales.reserva.bean.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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
          AND r.salida > :entrada
        """)
    boolean existeCruce(
            @Param("empresaId") Long empresaId,
            @Param("hostalId") Long hostalId,
            @Param("habitacionId") Long habitacionId,
            @Param("entrada") OffsetDateTime entrada,
            @Param("salida") OffsetDateTime salida
    );
}
