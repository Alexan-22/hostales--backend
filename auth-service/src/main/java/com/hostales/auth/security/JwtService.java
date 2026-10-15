package com.hostales.auth.security;

import com.hostales.auth.bean.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration-seconds}")
    private long expirationSeconds;

    @Value("${jwt.issuer}")
    private String issuer;

    public String generarToken(
            Usuario usuario,
            Set<Long> hostalesIds
    ) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(expirationSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .subject(usuario.getCorreo())
                .claim("usuarioId", usuario.getUsuarioId())
                .claim("empresaId", usuario.getEmpresaId())
                .claim(
                        "rol",
                        usuario.getRol().getNombre().name()
                )
                .claim("hostales", hostalesIds)
                .claim(
                        "nombreCompleto",
                        usuario.getNombre()
                                + " "
                                + usuario.getApellido()
                )
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .keyId("hostales-auth-key")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long obtenerExpiracionSegundos() {
        return expirationSeconds;
    }
}
