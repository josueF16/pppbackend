
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.upeu.PPPBACKEND.entity.CartaPresentacionPpp;

import java.util.List;

public interface CartaPresentacionPppRepository
        extends JpaRepository<CartaPresentacionPpp, Long> {

    List<CartaPresentacionPpp>
    findByIdSolicitudOrderByFechaCreacionDesc(
            Long idSolicitud
    );

    List<CartaPresentacionPpp> findByEstado(
            String estado
    );

    @Query(value = """
        SELECT ROL
        FROM USUARIO
        WHERE ID_USUARIO = :idUsuario
        """, nativeQuery = true)
    String obtenerRol(
            @Param("idUsuario") Long idUsuario
    );
}
