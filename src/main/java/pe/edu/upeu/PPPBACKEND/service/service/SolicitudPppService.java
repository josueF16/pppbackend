
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;

import java.util.List;

public interface SolicitudPppService {

    SolicitudPpp registrar(Long idUsuario, Long idEmpresa);

    List<SolicitudPpp> misSolicitudes(Long idUsuario);

    List<SolicitudPpp> listar(Long idCoordinador);

    SolicitudPppRepository.DetalleSolicitud detalle(
            Long idCoordinador, Long idSolicitud
    );

    SolicitudPpp validar(
            Long idCoordinador,
            Long idSolicitud,
            String estado,
            String comentario
    );
}
