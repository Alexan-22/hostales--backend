package com.hostales.auth.model;

import com.hostales.auth.bean.dto.CambiarEstadoUsuarioDto;
import com.hostales.auth.bean.dto.CrearUsuarioDto;
import com.hostales.auth.bean.dto.UsuarioResponseDto;
import com.hostales.auth.bean.entity.Rol;
import com.hostales.auth.bean.entity.Usuario;
import com.hostales.auth.bean.entity.UsuarioHostal;
import com.hostales.auth.persistence.RolRepository;
import com.hostales.auth.persistence.UsuarioHostalRepository;
import com.hostales.auth.persistence.UsuarioRepository;
import com.hostales.auth.usecase.UsuarioUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
    private final Validator validator;

    @Override
    @Transactional
    public UsuarioResponseDto registrarUsuario(
            Long empresaId,
            CrearUsuarioDto dto
    ) {
        validarId(empresaId, "empresa");
        validarDto(dto);

        if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña admite hasta 72 bytes UTF-8"
            );
        }

        Set<Long> hostales = dto.getHostalesIds() == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(dto.getHostalesIds());

        for (Long hostalId : hostales) {
            validarId(hostalId, "hostal");
        }

        hostalValidator.validarEmpresaActiva(empresaId);

        String correo = dto.getCorreo()
                .strip()
                .toLowerCase(Locale.ROOT);

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

        for (Long hostalId : hostales) {
            hostalValidator.validarHostalActivo(empresaId, hostalId);
        }

        Usuario usuario = Usuario.builder()
                .empresaId(empresaId)
                .rol(rol)
                .nombre(dto.getNombre().strip())
                .apellido(dto.getApellido().strip())
                .correo(correo)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
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
                    "No se pudo registrar el usuario por un conflicto de datos",
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarUsuarios(Long empresaId) {
        validarId(empresaId, "empresa");
        hostalValidator.validarEmpresaActiva(empresaId);

        return usuarioRepository
                .findByEmpresaIdOrderByApellidoAscNombreAsc(empresaId)
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
        validarId(empresaId, "empresa");
        validarId(usuarioId, "usuario");
        hostalValidator.validarEmpresaActiva(empresaId);

        return convertir(buscarUsuarioEntidad(empresaId, usuarioId));
    }

    @Override
    @Transactional
    public UsuarioResponseDto cambiarEstado(
            Long empresaId,
            Long usuarioId,
            CambiarEstadoUsuarioDto dto
    ) {
        validarId(empresaId, "empresa");
        validarId(usuarioId, "usuario");
        validarDto(dto);
        hostalValidator.validarEmpresaActiva(empresaId);

        Usuario usuario = buscarUsuarioEntidad(empresaId, usuarioId);
        usuario.setEstado(dto.getEstado());

        return convertir(usuarioRepository.saveAndFlush(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDto asignarHostal(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    ) {
        validarId(empresaId, "empresa");
        validarId(usuarioId, "usuario");
        validarId(hostalId, "hostal");

        hostalValidator.validarEmpresaActiva(empresaId);

        Usuario usuario = buscarUsuarioEntidad(empresaId, usuarioId);

        hostalValidator.validarHostalActivo(empresaId, hostalId);

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
        validarId(empresaId, "empresa");
        validarId(usuarioId, "usuario");
        validarId(hostalId, "hostal");

        hostalValidator.validarEmpresaActiva(empresaId);
        buscarUsuarioEntidad(empresaId, usuarioId);

        // No exige que el hostal siga activo para retirar el acceso.
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
        validarId(empresaId, "empresa");
        validarId(usuarioId, "usuario");

        return usuarioRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado en esta empresa"
                ));
    }

    private <T> void validarDto(T dto) {
        if (dto == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El cuerpo de la solicitud es obligatorio"
            );
        }

        Set<ConstraintViolation<T>> errores = validator.validate(dto);

        if (!errores.isEmpty()) {
            String mensaje = errores.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining("; "));

            throw error(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private void validarId(Long id, String recurso) {
        if (id == null || id <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ID de " + recurso + " debe ser positivo"
            );
        }
    }

    private UsuarioResponseDto convertir(Usuario usuario) {
        Set<Long> hostales = usuarioHostalRepository
                .findByEmpresaIdAndUsuarioIdOrderByHostalIdAsc(
                        usuario.getEmpresaId(),
                        usuario.getUsuarioId()
                )
                .stream()
                .map(UsuarioHostal::getHostalId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

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
