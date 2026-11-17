
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PPPBACKEND.entity.AsignacionSupervisorPpp;

import java.util.Optional;

public interface AsignacionSupervisorPppRepository
        extends JpaRepository<AsignacionSupervisorPpp, Long> {

    Optional<AsignacionSupervisorPpp> findByIdSolicitud(
            Long idSolicitud
    );
}
