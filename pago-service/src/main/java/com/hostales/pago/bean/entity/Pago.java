package com.hostales.pago.bean.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "pagos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_pago_empresa_clave_operacion",
                        columnNames = {
                                "empresa_id",
                                "clave_operacion"
                        }
                )
        }
)
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pago_id")
    private Long pagoId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "hostal_id", nullable = false)
    private Long hostalId;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(
            name = "monto",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal monto;

    @Column(
            name = "moneda",
            nullable = false,
            length = 3
    )
    private String moneda;

    @Column(
            name = "metodo",
            nullable = false,
            length = 20
    )
    private String metodo;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Builder.Default
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private String estado = "REGISTRADO";

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "pagado_en", nullable = false)
    private OffsetDateTime pagadoEn;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime creadoEn;

    @Column(
            name = "clave_operacion",
            nullable = false,
            updatable = false
    )
    private UUID claveOperacion;

    @Column(name = "anulado_en")
    private OffsetDateTime anuladoEn;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @PrePersist
    private void antesDeGuardar() {
        OffsetDateTime ahora = OffsetDateTime.now();

        if (pagadoEn == null) {
            pagadoEn = ahora;
        }

        if (creadoEn == null) {
            creadoEn = ahora;
        }

        if (estado == null) {
            estado = "REGISTRADO";
        }
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Pago otro)) {
            return false;
        }

        return getPagoId() != null
                && getPagoId().equals(otro.getPagoId());
    }

    @Override
    public int hashCode() {
        return Pago.class.hashCode();
    }
}
