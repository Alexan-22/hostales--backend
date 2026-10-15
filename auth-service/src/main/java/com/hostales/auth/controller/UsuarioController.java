package com.hostales.auth.controller;

import com.hostales.auth.bean.dto.*;
import com.hostales.auth.usecase.UsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas/{empresaId}/usuarios")
@PreAuthorize("""
        hasRole('ADMIN_EMPRESA')
        and @seguridadEmpresa.pertenece(#empresaId, authentication)
        """)
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> registrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody CrearUsuarioDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        usuarioUseCase.registrarUsuario(
                                empresaId,
                                dto
                        )
                );
    }

    @GetMapping
    public List<UsuarioResponseDto> listar(
            @PathVariable Long empresaId
    ) {
        return usuarioUseCase.listarUsuarios(empresaId);
    }

    @GetMapping("/{usuarioId}")
    public UsuarioResponseDto buscar(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId
    ) {
        return usuarioUseCase.buscarUsuario(
                empresaId,
                usuarioId
        );
    }

    @PatchMapping("/{usuarioId}/estado")
    public UsuarioResponseDto cambiarEstado(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId,
            @Valid @RequestBody CambiarEstadoUsuarioDto dto
    ) {
        return usuarioUseCase.cambiarEstado(
                empresaId,
                usuarioId,
                dto
        );
    }

    @PostMapping("/{usuarioId}/hostales/{hostalId}")
    public UsuarioResponseDto asignarHostal(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId,
            @PathVariable Long hostalId
    ) {
        return usuarioUseCase.asignarHostal(
                empresaId,
                usuarioId,
                hostalId
        );
    }

    @DeleteMapping("/{usuarioId}/hostales/{hostalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirarHostal(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId,
            @PathVariable Long hostalId
    ) {
        usuarioUseCase.retirarHostal(
                empresaId,
                usuarioId,
                hostalId
        );
    }
}
