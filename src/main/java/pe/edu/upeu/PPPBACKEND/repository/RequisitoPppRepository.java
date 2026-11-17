
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PPPBACKEND.entity.RequisitoPpp;

import java.util.List;

public interface RequisitoPppRepository
        extends JpaRepository<RequisitoPpp, Long> {

    List<RequisitoPpp> findAllByOrderByIdRequisitoAsc();

    @Query(value = """
        SELECT
            r.ID_REQUISITO AS "idRequisito",
            r.NOMBRE AS "nombre",
            r.DESCRIPCION AS "descripcion",
            CASE
                WHEN re.CUMPLE = 'S' THEN 'CUMPLE'
                ELSE 'PENDIENTE'
            END AS "estado"
        FROM REQUISITO_PPP r
        LEFT JOIN REQUISITO_ESTUDIANTE re
            ON r.ID_REQUISITO = re.ID_REQUISITO
            AND re.ID_USUARIO = :idUsuario
        ORDER BY r.ID_REQUISITO
        """, nativeQuery = true)
    List<EstadoRequisito> listarPorEstudiante(
            @Param("idUsuario") Long idUsuario
    );

    interface EstadoRequisito {
        Long getIdRequisito();
        String getNombre();
        String getDescripcion();
        String getEstado();
    }
}
