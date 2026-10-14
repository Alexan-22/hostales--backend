package com.hostales.hostal.persistence;

import com.hostales.hostal.bean.entity.Hostal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HostalRepository extends JpaRepository<Hostal, Long> {

    List<Hostal> findByEmpresa_EmpresaId(Long empresaId);

    Optional<Hostal> findByHostalIdAndEmpresa_EmpresaId(
            Long hostalId, Long empresaId
    );

    boolean existsByEmpresa_EmpresaIdAndNombre(
            Long empresaId, String nombre
    );
}