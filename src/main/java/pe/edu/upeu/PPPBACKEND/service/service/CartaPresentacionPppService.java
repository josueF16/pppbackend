
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.CartaPresentacionPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;

import java.util.List;

public interface CartaPresentacionPppService {

    List<SolicitudPpp> solicitudesAprobadas(Long idSecretaria);

    CartaPresentacionPpp crearBorrador(
            Long idSecretaria,
            Long idSolicitud,
            String modelo
    );

    CartaPresentacionPpp guardarBorrador(
            Long idSecretaria,
            Long idCarta,
            String contenido
    );

    CartaPresentacionPpp obtener(
            Long idSecretaria,
            Long idCarta
    );

    CartaPresentacionPpp enviar(
            Long idSecretaria,
            Long idCarta
    );

    CartaPresentacionPpp cancelarEnvio(
            Long idSecretaria,
            Long idCarta
    );

    List<CartaPresentacionPpp> cartasRecibidas(
            Long idEstudiante
    );
}
