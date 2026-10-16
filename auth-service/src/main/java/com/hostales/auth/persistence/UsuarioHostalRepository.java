package com.hostales.auth.persistence;

import com.hostales.auth.bean.entity.UsuarioHostal;
import com.hostales.auth.bean.entity.UsuarioHostalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UsuarioHostalRepository
        extends JpaRepository<UsuarioHostal, UsuarioHostalId> {

    List<UsuarioHostal> findByEmpresaIdAndUsuarioIdOrderByHostalIdAsc(
            Long empresaId,
            Long usuarioId
    );

    boolean existsByEmpresaIdAndUsuarioIdAndHostalId(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    );

    @Transactional
    long deleteByEmpresaIdAndUsuarioIdAndHostalId(
            Long empresaId,
            Long usuarioId,
            Long hostalId
    );

    @Transactional
    long deleteByEmpresaIdAndUsuarioId(
            Long empresaId,
            Long usuarioId
    );
}