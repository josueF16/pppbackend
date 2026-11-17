
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.DocumentoCartaPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.DocumentoCartaPppService;

import java.net.URI;
import java.util.List;

@Service
public class DocumentoCartaPppServiceImpl
        implements DocumentoCartaPppService {

    private final DocumentoCartaPppRepository repository;
    private final SolicitudPppRepository solicitudRepository;

    public DocumentoCartaPppServiceImpl(
            DocumentoCartaPppRepository repository,
            SolicitudPppRepository solicitudRepository) {

        this.repository = repository;
        this.solicitudRepository = solicitudRepository;
    }

    private void verificarEstudiante(Long idEstudiante) {

        String rol = solicitudRepository.obtenerRol(idEstudiante);

        if (!"ESTUDIANTE".equals(rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso exclusivo para estudiantes"
            );
        }
    }

    private void verificarSolicitud(
            Long idEstudiante,
            Long idSolicitud) {

        verificarEstudiante(idEstudiante);

        SolicitudPpp solicitud = solicitudRepository
                .findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (!solicitud.getIdUsuario().equals(idEstudiante)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La solicitud no pertenece al estudiante"
            );
        }

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud no esta aprobada"
            );
        }
    }

    private boolean enlaceValido(
            String driveFileId,
            String urlDocumento) {

        if (driveFileId == null ||
                !driveFileId.matches("[A-Za-z0-9_-]{10,200}")
                || urlDocumento == null) {
            return false;
        }

        try {
            URI uri = URI.create(urlDocumento);

            String host = uri.getHost();
            String path = uri.getPath();

            return "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && (host.equalsIgnoreCase("drive.google.com")
                    || host.equalsIgnoreCase("docs.google.com"))
                    && path != null
                    && path.contains("/d/" + driveFileId + "/");

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public DocumentoCartaPpp registrar(
            Long idEstudiante,
            Long idSolicitud,
            String tipoDocumento,
            String nombreArchivo,
            String driveFileId,
            String urlDocumento) {

        verificarSolicitud(idEstudiante, idSolicitud);

        if (!"ACEPTACION".equals(tipoDocumento) &&
                !"CONVENIO".equals(tipoDocumento)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de documento invalido"
            );
        }

        if (nombreArchivo == null || nombreArchivo.isBlank()
                || nombreArchivo.length() > 200
                || !enlaceValido(driveFileId, urlDocumento)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos del documento invalidos"
            );
        }

        if (repository.findByIdSolicitudAndTipoDocumento(
                idSolicitud, tipoDocumento).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este documento ya fue registrado"
            );
        }

        DocumentoCartaPpp documento = new DocumentoCartaPpp();

        documento.setIdSolicitud(idSolicitud);
        documento.setTipoDocumento(tipoDocumento);
        documento.setNombreArchivo(nombreArchivo);
        documento.setDriveFileId(driveFileId);
        documento.setUrlDocumento(urlDocumento);

        return repository.save(documento);
    }

    @Override
    public List<DocumentoCartaPpp> listar(
            Long idEstudiante,
            Long idSolicitud) {

        verificarSolicitud(idEstudiante, idSolicitud);

        return repository
                .findByIdSolicitudOrderByFechaRegistroDesc(
                        idSolicitud
                );
    }

    @Override
    public DocumentoCartaPpp obtener(
            Long idEstudiante,
            Long idDocumento) {

        DocumentoCartaPpp documento = repository
                .findById(idDocumento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Documento no encontrado"
                ));

        verificarSolicitud(
                idEstudiante,
                documento.getIdSolicitud()
        );

        return documento;
    }
}
