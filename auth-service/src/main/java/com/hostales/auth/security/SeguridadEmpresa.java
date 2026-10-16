package com.hostales.auth.security;

import com.hostales.auth.persistence.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Component("seguridadEmpresa")
@RequiredArgsConstructor
public class SeguridadEmpresa {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public boolean pertenece(
            Long empresaId,
            Authentication authentication
    ) {
        if (empresaId == null
                || empresaId <= 0
                || authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        Long empresaToken = obtenerId(jwt, "empresaId");
        Long usuarioId = obtenerId(jwt, "usuarioId");

        if (usuarioId == null || !Objects.equals(empresaId, empresaToken)) {
            return false;
        }

        return usuarioRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getEstado()))
                .filter(usuario -> Objects.equals(
                        usuario.getRol().getNombre().name(),
                        jwt.getClaimAsString("rol")
                ))
                .isPresent();
    }

    private Long obtenerId(Jwt jwt, String nombre) {
        Object valor = jwt.getClaims().get(nombre);

        if (!(valor instanceof Number numero)) {
            return null;
        }

        try {
            long id = new BigDecimal(numero.toString()).longValueExact();
            return id > 0 ? id : null;
        } catch (NumberFormatException | ArithmeticException ex) {
            return null;
        }
    }
}
