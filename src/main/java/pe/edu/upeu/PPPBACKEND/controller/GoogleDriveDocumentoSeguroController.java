
package pe.edu.upeu.PPPBACKEND.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;

import pe.edu.upeu.PPPBACKEND.repository.DocumentoCartaPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;

import pe.edu.upeu.PPPBACKEND.service.impl.GoogleDriveOAuthService;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/google/drive/documentos")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class GoogleDriveDocumentoSeguroController {

    private static final int MAX_BYTES = 5 * 1024 * 1024;

    private final DocumentoCartaPppRepository documentoRepository;
    private final SolicitudPppRepository solicitudRepository;
    private final GoogleDriveOAuthService googleOAuthService;

    private final RestClient restClient = RestClient.create();

    public GoogleDriveDocumentoSeguroController(
            DocumentoCartaPppRepository documentoRepository,
            SolicitudPppRepository solicitudRepository,
            GoogleDriveOAuthService googleOAuthService) {

        this.documentoRepository = documentoRepository;
        this.solicitudRepository = solicitudRepository;
        this.googleOAuthService = googleOAuthService;
    }

    // ==========================================
    // CU010 - DESCARGAR CARTA DE ACEPTACION
    // ==========================================

    @GetMapping("/{idDocumento}/pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long idDocumento,
            @AuthenticationPrincipal Jwt jwt) {

        // 1. Identificar al estudiante mediante JWT.
        if (jwt == null || jwt.getSubject() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesion"
            );
        }

        Long idEstudiante;

        try {
            idEstudiante = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario invalido"
            );
        }

        // 2. Buscar documento registrado en Oracle.
        DocumentoCartaPpp documento = documentoRepository
                .findById(idDocumento)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Documento no encontrado"
                        )
                );

        // 3. Verificar que sea carta de aceptacion.
        if (!"ACEPTACION".equals(documento.getTipoDocumento())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El documento no es una carta de aceptacion"
            );
        }

        // 4. Verificar la solicitud del documento.
        SolicitudPpp solicitud = solicitudRepository
                .findById(documento.getIdSolicitud())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitud no encontrada"
                        )
                );

        // 5. Impedir descargar documentos ajenos.
        if (!idEstudiante.equals(solicitud.getIdUsuario())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes descargar documentos de otro estudiante"
            );
        }

        // 6. Validar ID del archivo de Google Drive.
        String driveFileId = documento.getDriveFileId();

        if (
                driveFileId == null ||
                        !driveFileId.matches("[A-Za-z0-9_-]{10,200}")
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID de Google Drive invalido"
            );
        }

        // 7. Obtener token del backend.
        String tokenGoogle =
                googleOAuthService.obtenerTokenAccesoParaSubida();

        // 8. Recuperar PDF desde Google Drive.
        byte[] contenido;

        try {
            contenido = restClient.get()
                    .uri(
                            "https://www.googleapis.com/drive/v3/files/{id}?alt=media",
                            driveFileId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(tokenGoogle)
                    )
                    .retrieve()
                    .body(byte[].class);

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo recuperar el documento de Google Drive"
            );
        }

        // 9. Validar la respuesta.
        if (
                contenido == null ||
                        contenido.length < 5 ||
                        contenido.length > MAX_BYTES ||
                        contenido[0] != '%' ||
                        contenido[1] != 'P' ||
                        contenido[2] != 'D' ||
                        contenido[3] != 'F' ||
                        contenido[4] != '-'
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Drive no devolvio un PDF valido"
            );
        }

        // 10. Preparar nombre seguro.
        String nombre = documento.getNombreArchivo();

        if (nombre == null || nombre.isBlank()) {
            nombre = "carta_aceptacion.pdf";
        }

        nombre = nombre
                .replace('\\', '_')
                .replace('/', '_')
                .replaceAll("[\\p{Cntrl}]", "_");

        if (!nombre.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            nombre += ".pdf";
        }

        String disposicion = ContentDisposition
                .attachment()
                .filename(nombre, StandardCharsets.UTF_8)
                .build()
                .toString();

        // 11. Devolver PDF al estudiante.
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposicion
                )
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(contenido.length)
                .body(contenido);
    }
}
