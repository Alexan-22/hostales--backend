package com.hostales.reserva.persistence;

import com.hostales.reserva.bean.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    List<Huesped> findByEmpresaIdOrderByApellidoAscNombreAsc(Long empresaId);

    Optional<Huesped> findByHuespedIdAndEmpresaId(
            Long huespedId, Long empresaId
    );

    boolean existsByEmpresaIdAndTipoDocumentoAndNumeroDocumento(
            Long empresaId, String tipoDocumento, String numeroDocumento
    );
}
