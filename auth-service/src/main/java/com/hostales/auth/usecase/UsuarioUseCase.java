package com.hostales.auth.usecase;

import com.hostales.auth.bean.dto.CambiarEstadoUsuarioDto;
import com.hostales.auth.bean.dto.CrearUsuarioDto;
import com.hostales.auth.bean.dto.UsuarioResponseDto;

import java.util.List;

public interface UsuarioUseCase {

    UsuarioResponseDto registrarUsuario(
            Long empresaId,
            CrearUsuarioDto dto
    );

    List<UsuarioResponseDto> listarUsuarios(Long empresaId);

    UsuarioResponseDto buscarUsuario(
            Long empresaId,
            Long usuarioId
    );

    UsuarioResponseDto cambiarEstado(
            Long empresaId,
            Long usuarioId,
            CambiarEstadoUsuarioDto dto
    );

    UsuarioResponseDto asignarHostal(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    );

    void retirarHostal(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    );
}
