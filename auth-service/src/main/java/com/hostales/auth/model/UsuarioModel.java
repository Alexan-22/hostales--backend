package com.hostales.auth.model;

import com.hostales.auth.bean.dto.*;
import com.hostales.auth.bean.entity.*;
import com.hostales.auth.persistence.*;
import com.hostales.auth.usecase.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioModel implements UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioHostalRepository usuarioHostalRepository;
    private final PasswordEncoder passwordEncoder;
    private final HostalValidator hostalValidator;

    @Override
    @Transactional
    public UsuarioResponseDto registrarUsuario(
            Long empresaId,
            CrearUsuarioDto dto
    ) {
        hostalValidator.validarEmpresaActiva(empresaId);

        String correo = dto.getCorreo()
                .trim()
                .toLowerCase();

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado"
            );
        }

        Rol rol = rolRepository.findByNombre(dto.getRol())
                .orElseThrow(() -> error(
                        HttpStatus.BAD_REQUEST,
                        "El rol solicitado no existe"
                ));

        Set<Long> hostales = dto.getHostalesIds() == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(dto.getHostalesIds());

        for (Long hostalId : hostales) {
            hostalValidator.validarHostalActivo(
                    empresaId,
                    hostalId
            );
        }

        Usuario usuario = Usuario.builder()
                .empresaId(empresaId)
                .rol(rol)
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .correo(correo)
                .passwordHash(
                        passwordEncoder.encode(dto.getPassword())
                )
                .estado(true)
                .build();

        try {
            usuario = usuarioRepository.saveAndFlush(usuario);

            for (Long hostalId : hostales) {
                usuarioHostalRepository.save(
                        UsuarioHostal.builder()
                                .empresaId(empresaId)
                                .usuarioId(usuario.getUsuarioId())
                                .hostalId(hostalId)
                                .build()
                );
            }

            usuarioHostalRepository.flush();

            return convertir(usuario, hostales);

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo registrar el usuario",
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarUsuarios(
            Long empresaId
    ) {
        hostalValidator.validarEmpresaActiva(empresaId);

        return usuarioRepository
                .findByEmpresaIdOrderByApellidoAscNombreAsc(
                        empresaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto buscarUsuario(
            Long empresaId,
            Long usuarioId
    ) {
        return convertir(
                buscarUsuarioEntidad(empresaId, usuarioId)
        );
    }

    @Override
    @Transactional
    public UsuarioResponseDto cambiarEstado(
            Long empresaId,
            Long usuarioId,
            CambiarEstadoUsuarioDto dto
    ) {
        Usuario usuario = buscarUsuarioEntidad(
                empresaId,
                usuarioId
        );

        usuario.setEstado(dto.getEstado());

        return convertir(
                usuarioRepository.saveAndFlush(usuario)
        );
    }

    @Override
    @Transactional
    public UsuarioResponseDto asignarHostal(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    ) {
        Usuario usuario = buscarUsuarioEntidad(
                empresaId,
                usuarioId
        );

        hostalValidator.validarHostalActivo(
                empresaId,
                hostalId
        );

        boolean existe = usuarioHostalRepository
                .existsByEmpresaIdAndUsuarioIdAndHostalId(
                        empresaId,
                        usuarioId,
                        hostalId
                );

        if (!existe) {
            usuarioHostalRepository.saveAndFlush(
                    UsuarioHostal.builder()
                            .empresaId(empresaId)
                            .usuarioId(usuarioId)
                            .hostalId(hostalId)
                            .build()
            );
        }

        return convertir(usuario);
    }

    @Override
    @Transactional
    public void retirarHostal(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    ) {
        buscarUsuarioEntidad(empresaId, usuarioId);

        long eliminados = usuarioHostalRepository
                .deleteByEmpresaIdAndUsuarioIdAndHostalId(
                        empresaId,
                        usuarioId,
                        hostalId
                );

        if (eliminados == 0) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "El usuario no tenía acceso a ese hostal"
            );
        }
    }

    private Usuario buscarUsuarioEntidad(
            Long empresaId,
            Long usuarioId
    ) {
        if (empresaId == null || empresaId <= 0
                || usuarioId == null || usuarioId <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Los identificadores deben ser positivos"
            );
        }

        return usuarioRepository
                .findByUsuarioIdAndEmpresaId(
                        usuarioId,
                        empresaId
                )
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado en esta empresa"
                ));
    }

    private UsuarioResponseDto convertir(Usuario usuario) {
        Set<Long> hostales = usuarioHostalRepository
                .findByEmpresaIdAndUsuarioIdOrderByHostalIdAsc(
                        usuario.getEmpresaId(),
                        usuario.getUsuarioId()
                )
                .stream()
                .map(UsuarioHostal::getHostalId)
                .collect(Collectors.toCollection(
                        LinkedHashSet::new
                ));

        return convertir(usuario, hostales);
    }

    private UsuarioResponseDto convertir(
            Usuario usuario,
            Set<Long> hostales
    ) {
        return UsuarioResponseDto.builder()
                .usuarioId(usuario.getUsuarioId())
                .empresaId(usuario.getEmpresaId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .estado(usuario.getEstado())
                .hostalesIds(hostales)
                .creadoEn(usuario.getCreadoEn())
                .build();
    }

    private ResponseStatusException error(
            HttpStatus estado,
            String mensaje
    ) {
        return new ResponseStatusException(estado, mensaje);
    }
}
