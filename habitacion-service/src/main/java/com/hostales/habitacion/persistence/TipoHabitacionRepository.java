package com.hostales.habitacion.persistence;

import com.hostales.habitacion.bean.entity.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {

    List<TipoHabitacion> findByEmpresaIdAndHostalIdOrderByNombreAsc(
            Long empresaId, Long hostalId
    );

    Optional<TipoHabitacion> findByTipoHabitacionIdAndEmpresaIdAndHostalId(
            Long tipoHabitacionId, Long empresaId, Long hostalId
    );

    boolean existsByEmpresaIdAndHostalIdAndNombre(
            Long empresaId, Long hostalId, String nombre
    );
}
