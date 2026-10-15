package com.hostales.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("seguridadEmpresa")
public class SeguridadEmpresa {

    public boolean pertenece(
            Long empresaId,
            Authentication authentication
    ) {
        if (empresaId == null
                || authentication == null
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        Number empresaToken = jwt.getClaim("empresaId");

        return empresaToken != null
                && Objects.equals(
                empresaToken.longValue(),
                empresaId
        );
    }
}
