package com.hostales.auth.persistence;

import com.hostales.auth.bean.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByUsuarioIdAndEmpresaId(
            Long usuarioId,
            Long empresaId
    );

    List<Usuario> findByEmpresaIdOrderByApellidoAscNombreAsc(
            Long empresaId
    );

    boolean existsByCorreoIgnoreCase(String correo);
}
