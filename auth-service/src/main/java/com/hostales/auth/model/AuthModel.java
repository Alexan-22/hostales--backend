package com.hostales.auth.model;

import com.hostales.auth.bean.dto.LoginDto;
import com.hostales.auth.bean.dto.LoginResponseDto;
import com.hostales.auth.bean.dto.UsuarioSesionDto;
import com.hostales.auth.bean.entity.Usuario;
import com.hostales.auth.bean.entity.UsuarioHostal;
import com.hostales.auth.persistence.UsuarioHostalRepository;
import com.hostales.auth.persistence.UsuarioRepository;
import com.hostales.auth.security.JwtService;
import com.hostales.auth.usecase.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthModel implements AuthUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioHostalRepository usuarioHostalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HostalValidator hostalValidator;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginDto dto) {
        if (dto == null
                || dto.getCorreo() == null
                || dto.getCorreo().isBlank()
                || dto.getPassword() == null
                || dto.getPassword().isBlank()) {
            throw credencialesInvalidas();
        }

        if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw credencialesInvalidas();
        }

        String correo = dto.getCorreo().strip().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(this::credencialesInvalidas);

        boolean passwordValida = passwordEncoder.matches(
                dto.getPassword(),
                usuario.getPasswordHash()
        );

        if (!passwordValida || !Boolean.TRUE.equals(usuario.getEstado())) {
            throw credencialesInvalidas();
        }

        hostalValidator.validarEmpresaActiva(usuario.getEmpresaId());

        Set<Long> hostales = obtenerHostales(usuario);

        return LoginResponseDto.builder()
                .accessToken(jwtService.generarToken(usuario, hostales))
                .tokenType("Bearer")
                .expiresIn(jwtService.obtenerExpiracionSegundos())
                .usuarioId(usuario.getUsuarioId())
                .empresaId(usuario.getEmpresaId())
                .nombreCompleto(nombreCompleto(usuario))
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .hostalesPermitidos(hostales)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioSesionDto obtenerSesionActual() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw sesionInvalida();
        }

        Long usuarioId = obtenerId(jwt, "usuarioId");
        Long empresaId = obtenerId(jwt, "empresaId");

        Usuario usuario = usuarioRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(this::sesionInvalida);

        if (!Boolean.TRUE.equals(usuario.getEstado())
                || !Objects.equals(
                usuario.getRol().getNombre().name(),
                jwt.getClaimAsString("rol")
        )) {
            throw sesionInvalida();
        }

        hostalValidator.validarEmpresaActiva(empresaId);

        return UsuarioSesionDto.builder()
                .usuarioId(usuario.getUsuarioId())
                .empresaId(usuario.getEmpresaId())
                .nombreCompleto(nombreCompleto(usuario))
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .hostalesPermitidos(obtenerHostales(usuario))
                .build();
    }

    private Set<Long> obtenerHostales(Usuario usuario) {
        return usuarioHostalRepository
                .findByEmpresaIdAndUsuarioIdOrderByHostalIdAsc(
                        usuario.getEmpresaId(),
                        usuario.getUsuarioId()
                )
                .stream()
                .map(UsuarioHostal::getHostalId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Long obtenerId(Jwt jwt, String nombre) {
        Object valor = jwt.getClaims().get(nombre);

        if (!(valor instanceof Number numero)) {
            throw sesionInvalida();
        }

        try {
            long id = new java.math.BigDecimal(numero.toString())
                    .longValueExact();

            if (id <= 0) {
                throw sesionInvalida();
            }

            return id;
        } catch (NumberFormatException | ArithmeticException ex) {
            throw sesionInvalida();
        }
    }

    private String nombreCompleto(Usuario usuario) {
        return usuario.getNombre() + " " + usuario.getApellido();
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos"
        );
    }

    private ResponseStatusException sesionInvalida() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "La sesión ya no es válida. Inicia sesión nuevamente"
        );
    }
}