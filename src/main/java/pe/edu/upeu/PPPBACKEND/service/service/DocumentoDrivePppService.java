
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoDrivePpp;

public interface DocumentoDrivePppService {

    DocumentoDrivePpp guardar(
            Long idCoordinador,
            Long idSolicitud,
            String driveFileId,
            String urlDocumento,
            String urlVistaPrevia
    );

    DocumentoDrivePpp consultar(
            Long idCoordinador,
            Long idSolicitud
    );
}
