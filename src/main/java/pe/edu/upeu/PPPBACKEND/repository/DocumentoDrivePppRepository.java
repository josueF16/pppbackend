
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PPPBACKEND.entity.DocumentoDrivePpp;

public interface DocumentoDrivePppRepository
        extends JpaRepository<DocumentoDrivePpp, Long> {

    @Query(value = """
        SELECT ROL FROM USUARIO
        WHERE ID_USUARIO = :idUsuario
        """, nativeQuery = true)
    String obtenerRol(
            @Param("idUsuario") Long idUsuario
    );
}
