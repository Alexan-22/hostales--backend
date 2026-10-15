package com.hostales.auth.model;

import com.hostales.auth.bean.dto.*;
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

import java.util.LinkedHashSet;
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
        String correo = dto.getCorreo()
                .trim()
                .toLowerCase();

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(this::credencialesInvalidas);

        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario está inactivo"
            );
        }

        if (!passwordEncoder.matches(
                dto.getPassword(),
                usuario.getPasswordHash()
        )) {
            throw credencialesInvalidas();
        }

        hostalValidator.validarEmpresaActiva(
                usuario.getEmpresaId()
        );

        Set<Long> hostales = obtenerHostales(usuario);

        String token = jwtService.generarToken(
                usuario,
                hostales
        );

        return LoginResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(
                        jwtService.obtenerExpiracionSegundos()
                )
                .usuarioId(usuario.getUsuarioId())
                .empresaId(usuario.getEmpresaId())
                .nombreCompleto(
                        usuario.getNombre()
                                + " "
                                + usuario.getApellido()
                )
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .hostalesPermitidos(hostales)
                .build();
    }

    @Override
    public UsuarioSesionDto obtenerSesionActual() {
        Authentication autenticacion =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (autenticacion == null
                || !(autenticacion.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No existe una sesión autenticada"
            );
        }

        Set<Long> hostales = new LinkedHashSet<>();

        Object claim = jwt.getClaim("hostales");

        if (claim instanceof Iterable<?> valores) {
            for (Object valor : valores) {
                if (valor instanceof Number numero) {
                    hostales.add(numero.longValue());
                }
            }
        }

        return UsuarioSesionDto.builder()
                .usuarioId(

