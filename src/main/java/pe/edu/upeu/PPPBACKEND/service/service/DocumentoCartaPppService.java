
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;
import java.util.List;

public interface DocumentoCartaPppService {

    DocumentoCartaPpp registrar(
            Long idEstudiante,
            Long idSolicitud,
            String tipoDocumento,
            String nombreArchivo,
            String driveFileId,
            String urlDocumento
    );

    List<DocumentoCartaPpp> listar(
            Long idEstudiante,
            Long idSolicitud
    );

    DocumentoCartaPpp obtener(
            Long idEstudiante,
            Long idDocumento
    );
}
