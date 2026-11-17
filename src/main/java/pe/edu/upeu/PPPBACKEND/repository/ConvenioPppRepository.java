
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PPPBACKEND.entity.ConvenioPpp;

import java.util.Optional;

public interface ConvenioPppRepository
        extends JpaRepository<ConvenioPpp, Long> {

    Optional<ConvenioPpp> findByIdSolicitud(
            Long idSolicitud
    );
}
