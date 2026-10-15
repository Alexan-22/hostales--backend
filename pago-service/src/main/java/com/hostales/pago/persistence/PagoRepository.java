package com.hostales.pago.persistence;

import com.hostales.pago.bean.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago>
    findByEmpresaIdAndHostalIdAndReservaIdOrderByPagadoEnAscPagoIdAsc(
            Long empresaId,
            Long hostalId,
            Long reservaId
    );

    Optional<Pago>
    findByPagoIdAndEmpresaIdAndHostalIdAndReservaId(
            Long pagoId,
            Long empresaId,
            Long hostalId,
            Long reservaId
    );

    Optional<Pago> findByEmpresaIdAndClaveOperacion(
            Long empresaId,
            UUID claveOperacion
    );

    @Query("""
            SELECT COALESCE(SUM(p.monto), 0)
            FROM Pago p
            WHERE p.empresaId = :empresaId
              AND p.hostalId = :hostalId
              AND p.reservaId = :reservaId
              AND p.estado = 'REGISTRADO'
            """)
    BigDecimal sumarPagosRegistrados(
            @Param("empresaId") Long empresaId,
            @Param("hostalId") Long hostalId,
            @Param("reservaId") Long reservaId
    );
}