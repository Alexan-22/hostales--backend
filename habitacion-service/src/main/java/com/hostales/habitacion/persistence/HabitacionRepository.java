package com.hostales.habitacion.persistence;

import com.hostales.habitacion.bean.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByEmpresaIdAndHostalIdOrderByNumeroAsc(
            Long empresaId, Long hostalId
    );

    Optional<Habitacion> findByHabitacionIdAndEmpresaIdAndHostalId(
            Long habitacionId, Long empresaId, Long hostalId
    );

    boolean existsByEmpresaIdAndHostalIdAndNumero(
            Long empresaId, Long hostalId, String numero
    );
}
