
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;

import java.util.List;
import java.util.Optional;

public interface DocumentoCartaPppRepository
        extends JpaRepository<DocumentoCartaPpp, Long> {

    List<DocumentoCartaPpp>
    findByIdSolicitudOrderByFechaRegistroDesc(
            Long idSolicitud
    );

    Optional<DocumentoCartaPpp>
    findByIdSolicitudAndTipoDocumento(
            Long idSolicitud,
            String tipoDocumento
    );
}
