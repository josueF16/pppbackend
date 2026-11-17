
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;

import java.time.LocalDateTime;
import java.util.List;

public interface SolicitudPppRepository
        extends JpaRepository<SolicitudPpp, Long> {

    List<SolicitudPpp> findAllByOrderByFechaSolicitudDesc();

    List<SolicitudPpp> findByIdUsuarioOrderByFechaSolicitudDesc(
            Long idUsuario
    );

    @Query(value = """
        SELECT ROL
        FROM USUARIO
        WHERE ID_USUARIO = :idUsuario
        """, nativeQuery = true)
    String obtenerRol(@Param("idUsuario") Long idUsuario);

    @Query(value = """
        SELECT
            s.ID_SOLICITUD AS "idSolicitud",
            u.ID_USUARIO AS "idUsuario",
            u.NOMBRES AS "nombres",
            u.APELLIDOS AS "apellidos",
            u.DNI AS "dni",
            u.CORREO AS "correo",
            e.NOMBRE AS "empresa",
            s.ESTADO AS "estado",
            s.COMENTARIO AS "comentario",
            s.FECHA_SOLICITUD AS "fechaSolicitud",
            s.FECHA_REVISION AS "fechaRevision"
        FROM SOLICITUD_PPP s
        JOIN USUARIO u ON u.ID_USUARIO = s.ID_USUARIO
        JOIN EMPRESA_PPP e ON e.ID_EMPRESA = s.ID_EMPRESA
        WHERE s.ID_SOLICITUD = :idSolicitud
        """, nativeQuery = true)
    DetalleSolicitud obtenerDetalle(
            @Param("idSolicitud") Long idSolicitud
    );

    interface DetalleSolicitud {
        Long getIdSolicitud();
        Long getIdUsuario();
        String getNombres();
        String getApellidos();
        String getDni();
        String getCorreo();
        String getEmpresa();
        String getEstado();
        String getComentario();
        LocalDateTime getFechaSolicitud();
        LocalDateTime getFechaRevision();
    }
}
