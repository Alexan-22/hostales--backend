package com.hostales.auth.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");

        if (rol == null || rol.isBlank()) {
            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + rol)
        );
    }
}