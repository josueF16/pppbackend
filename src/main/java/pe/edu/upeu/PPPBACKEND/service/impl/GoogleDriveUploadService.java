
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.DocumentoCartaPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.DocumentoCartaPppService;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

@Service
public class GoogleDriveUploadService {

    private static final long MAX_PDF_BYTES =
            5L * 1024 * 1024;

    private static final String DRIVE_UPLOAD_URL =
            "https://www.googleapis.com/upload/drive/v3/files"
                    + "?uploadType=resumable"
                    + "&fields=id,name,mimeType,webViewLink";

    private final RestClient restClient = RestClient.create();

    private final GoogleDriveOAuthService googleOAuthService;
    private final DocumentoCartaPppService documentoService;
    private final SolicitudPppRepository solicitudRepository;
    private final DocumentoCartaPppRepository documentoRepository;

    public GoogleDriveUploadService(
            GoogleDriveOAuthService googleOAuthService,
            DocumentoCartaPppService documentoService,
            SolicitudPppRepository solicitudRepository,
            DocumentoCartaPppRepository documentoRepository) {

        this.googleOAuthService = googleOAuthService;
        this.documentoService = documentoService;
        this.solicitudRepository = solicitudRepository;
        this.documentoRepository = documentoRepository;
    }

    // ==========================================
    // SUBIR PDF Y REGISTRARLO EN ORACLE
    // ==========================================

    @PreAuthorize("hasRole('ESTUDIANTE')")
    public DocumentoCartaPpp subirDocumento(
            Long idEstudiante,
            Long idSolicitud,
            String tipoDocumento,
            MultipartFile archivo) {

        // 1. Verificar solicitud y propietario.
        verificarSolicitud(idEstudiante, idSolicitud);

        // 2. Verificar tipo de documento.
        if (!"ACEPTACION".equals(tipoDocumento)
                && !"CONVENIO".equals(tipoDocumento)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de documento invalido"
            );
        }

        // 3. Evitar documentos duplicados.
        if (documentoRepository
                .findByIdSolicitudAndTipoDocumento(
                        idSolicitud,
                        tipoDocumento
                ).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un documento de este tipo"
            );
        }

        // 4. Validar nombre, tamaño y PDF.
        String nombreArchivo = validarNombre(archivo);
        byte[] contenido = validarContenido(archivo);

        // 5. Obtener token valido de Google.
        String accessToken =
                googleOAuthService.obtenerTokenAccesoParaSubida();

        // 6. Subir archivo a Google Drive.
        String driveFileId = subirAGoogleDrive(
                accessToken,
                nombreArchivo,
                contenido
        );

        // 7. Construir enlace al documento privado.
        String urlDocumento =
                "https://drive.google.com/file/d/"
                        + driveFileId
                        + "/view";

        // 8. Registrar el documento en Oracle usando
        // el servicio existente y sus validaciones.
        try {

            return documentoService.registrar(
                    idEstudiante,
                    idSolicitud,
                    tipoDocumento,
                    nombreArchivo,
                    driveFileId,
                    urlDocumento
            );

        } catch (RuntimeException ex) {

            // Si falla Oracle, intentar borrar
            // el archivo que acabamos de subir.
            eliminarArchivoDeDrive(
                    accessToken,
                    driveFileId
            );

            throw ex;
        }
    }

    // ==========================================
    // VALIDAR SOLICITUD Y PROPIETARIO
    // ==========================================

    private void verificarSolicitud(
            Long idEstudiante,
            Long idSolicitud) {

        if (idEstudiante == null || idSolicitud == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identificadores invalidos"
            );
        }

        String rol = solicitudRepository
                .obtenerRol(idEstudiante);

        if (!"ESTUDIANTE".equals(rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Operacion exclusiva para estudiantes"
            );
        }

        SolicitudPpp solicitud = solicitudRepository
                .findById(idSolicitud)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitud no encontrada"
                        )
                );

        if (!idEstudiante.equals(solicitud.getIdUsuario())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La solicitud no pertenece al estudiante"
            );
        }

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud debe estar aprobada"
            );
        }
    }

    // ==========================================
    // VALIDAR NOMBRE DEL ARCHIVO
    // ==========================================

    private String validarNombre(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona un archivo PDF"
            );
        }

        String original = archivo.getOriginalFilename();

        if (original == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo no tiene nombre"
            );
        }

        // Evitar componentes de rutas locales.
        String nombre = original
                .replace('\\', '/');

        nombre = nombre.substring(
                nombre.lastIndexOf('/') + 1
        ).trim();

        if (nombre.isBlank()
                || nombre.length() > 200
                || !nombre.toLowerCase(Locale.ROOT)
                .endsWith(".pdf")
                || nombre.chars().anyMatch(
                Character::isISOControl
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de archivo PDF invalido"
            );
        }

        return nombre;
    }

    // ==========================================
    // VALIDAR CONTENIDO PDF
    // ==========================================

    private byte[] validarContenido(MultipartFile archivo) {

        if (archivo.getSize() <= 0
                || archivo.getSize() > MAX_PDF_BYTES) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El PDF no debe superar 5 MB"
            );
        }

        String tipo = archivo.getContentType();

        if (tipo != null
                && !MediaType.APPLICATION_PDF_VALUE
                .equalsIgnoreCase(tipo)
                && !"application/octet-stream"
                .equalsIgnoreCase(tipo)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo debe ser PDF"
            );
        }

        byte[] bytes;

        try {

            bytes = archivo.getBytes();

        } catch (IOException ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudo leer el archivo"
            );
        }

        // Comprobacion basica de la cabecera PDF.
        // No sustituye un analisis de seguridad.
        if (bytes.length < 5
                || bytes[0] != '%'
                || bytes[1] != 'P'
                || bytes[2] != 'D'
                || bytes[3] != 'F'
                || bytes[4] != '-') {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El contenido no tiene formato PDF valido"
            );
        }

        return bytes;
    }

    // ==========================================
    // SUBIR PDF A GOOGLE DRIVE
    // ==========================================

    private String subirAGoogleDrive(
            String accessToken,
            String nombreArchivo,
            byte[] contenido) {

        try {

            // 1. Crear una sesion de subida
            // con los metadatos del documento.
            ResponseEntity<Void> inicio = restClient.post()
                    .uri(DRIVE_UPLOAD_URL)
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken);
                        headers.set(
                                "X-Upload-Content-Type",
                                "application/pdf"
                        );
                        headers.set(
                                "X-Upload-Content-Length",
                                String.valueOf(contenido.length)
                        );
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "name", nombreArchivo,
                            "mimeType", "application/pdf"
                    ))
                    .retrieve()
                    .toBodilessEntity();

            // 2. Obtener URL de la sesion.
            URI urlSesion = inicio.getHeaders().getLocation();

            if (!urlSesionValida(urlSesion)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Google devolvio una sesion de subida invalida"
                );
            }

            // 3. Enviar bytes reales del PDF.
            Map<?, ?> respuesta = restClient.put()
                    .uri(urlSesion)
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(contenido.length)
                    .body(contenido)
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Google no devolvio los datos del archivo"
                );
            }

            // 4. Obtener el ID generado por Google.
            Object id = respuesta.get("id");

            if (!(id instanceof String fileId)
                    || !fileId.matches(
                    "[A-Za-z0-9_-]{10,200}"
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Google no devolvio un ID de archivo valido"
                );
            }

            return fileId;

        } catch (RestClientException ex) {

            // No exponer tokens ni detalles privados.
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo subir el PDF a Google Drive"
            );
        }
    }

    // ==========================================
    // VERIFICAR URL DE SESION GOOGLE
    // ==========================================

    private boolean urlSesionValida(URI uri) {

        if (uri == null
                || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null) {
            return false;
        }

        String host = uri.getHost()
                .toLowerCase(Locale.ROOT);

        // La URL debe pertenecer a la API de Google.
        return host.equals("www.googleapis.com")
                && uri.getPath() != null
                && uri.getPath().startsWith(
                "/upload/drive/v3/files"
        );
    }

    // ==========================================
    // REVERTIR UNA SUBIDA SI FALLA ORACLE
    // ==========================================

    private void eliminarArchivoDeDrive(
            String accessToken,
            String driveFileId) {

        try {

            restClient.delete()
                    .uri(
                            "https://www.googleapis.com/drive/v3/files/{id}",
                            driveFileId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(accessToken)
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {

            // La eliminacion es un intento de
            // compensacion. No ocultar el error
            // original del registro en Oracle.
            System.err.println(
                    "Advertencia: no se pudo revertir "
                            + "la subida del archivo en Google Drive."
            );
        }
    }
}
