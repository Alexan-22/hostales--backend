package com.hostales.auth.persistence;

import com.hostales.auth.bean.entity.Rol;
import com.hostales.auth.bean.enums.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);
}

