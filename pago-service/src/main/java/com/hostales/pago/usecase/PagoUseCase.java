package com.hostales.pago.usecase;

import com.hostales.pago.bean.dto.AnularPagoDto;
import com.hostales.pago.bean.dto.PagoDto;
import com.hostales.pago.bean.dto.PagoResponseDto;
import com.hostales.pago.bean.dto.SaldoReservaDto;

import java.util.List;

public interface PagoUseCase {

    PagoResponseDto registrarPago(
            Long empresaId,
            Long hostalId,
            Long reservaId,
            PagoDto dto
    );

    List<PagoResponseDto> listarPagos(
            Long empresaId,
            Long hostalId,
            Long reservaId
    );

    SaldoReservaDto consultarSaldo(
            Long empresaId,
            Long hostalId,
            Long reservaId
    );

    PagoResponseDto anularPago(
            Long empresaId,
            Long hostalId,
            Long reservaId,
            Long pagoId,
            AnularPagoDto dto
    );
}