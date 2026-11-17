
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoDrivePpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.DocumentoDrivePppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.DocumentoDrivePppService;

import java.net.URI;

@Service
public class DocumentoDrivePppServiceImpl
        implements DocumentoDrivePppService {

    private final DocumentoDrivePppRepository repository;
    private final SolicitudPppRepository solicitudRepository;

    public DocumentoDrivePppServiceImpl(
            DocumentoDrivePppRepository repository,
            SolicitudPppRepository solicitudRepository) {
        this.repository = repository;
        this.solicitudRepository = solicitudRepository;
    }

    private void verificarCoordinador(Long idUsuario) {
        String rol = repository.obtenerRol(idUsuario);

        if (!"COORDINADOR_PPP".equals(rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso exclusivo del coordinador PPP"
            );
        }
    }

    private boolean esEnlaceDrive(String url) {
        try {
            URI uri = URI.create(url);

            String host = uri.getHost();

            return "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && (host.equalsIgnoreCase("drive.google.com")
                    || host.equalsIgnoreCase("docs.google.com"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public DocumentoDrivePpp guardar(
            Long idCoordinador,
            Long idSolicitud,
            String driveFileId,
            String urlDocumento,
            String urlVistaPrevia) {

        verificarCoordinador(idCoordinador);

        SolicitudPpp solicitud = solicitudRepository
                .findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud no esta aprobada"
            );
        }

        if (driveFileId == null || driveFileId.isBlank()
                || !esEnlaceDrive(urlDocumento)
                || !esEnlaceDrive(urlVistaPrevia)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos de Google Drive invalidos"
            );
        }

        if (repository.existsById(idSolicitud)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya tiene un documento vinculado"
            );
        }

        DocumentoDrivePpp documento = new DocumentoDrivePpp();

        documento.setIdSolicitud(idSolicitud);
        documento.setDriveFileId(driveFileId);
        documento.setUrlDocumento(urlDocumento);
        documento.setUrlVistaPrevia(urlVistaPrevia);

        return repository.save(documento);
    }

    @Override
    public DocumentoDrivePpp consultar(
            Long idCoordinador,
            Long idSolicitud) {

        verificarCoordinador(idCoordinador);

        return repository.findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Documento no encontrado"
                ));
    }
}
