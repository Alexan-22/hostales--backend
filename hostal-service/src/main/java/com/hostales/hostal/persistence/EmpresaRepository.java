package com.hostales.hostal.persistence;

import com.hostales.hostal.bean.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByRuc(String ruc);
}