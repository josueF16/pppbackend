
package pe.edu.upeu.PPPBACKEND.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import pe.edu.upeu.PPPBACKEND.service.impl.GoogleDriveOAuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/google/oauth")
public class GoogleDriveOAuthController {

    private final GoogleDriveOAuthService googleDriveOAuthService;

    public GoogleDriveOAuthController(
            GoogleDriveOAuthService googleDriveOAuthService) {

        this.googleDriveOAuthService = googleDriveOAuthService;
    }

    // ==========================================
    // 1. INICIAR AUTORIZACION GOOGLE DRIVE
    // ==========================================

    @GetMapping("/autorizar")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('COORDINADOR_PPP')")
    public Map<String, String> autorizar(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticacion requerida"
            );
        }

        Long idCoordinador;

        try {
            idCoordinador = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificador de usuario invalido"
            );
        }

        String url = googleDriveOAuthService
                .crearUrlAutorizacion(idCoordinador);

        return Map.of("urlAutorizacion", url);
    }

    // ==========================================
    // 2. RECIBIR RESPUESTA DE GOOGLE OAUTH
    // ==========================================

    @GetMapping(
            value = "/callback",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> callback(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error) {

        if (error != null && !error.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("""
                            <!DOCTYPE html>
                            <html lang="es">
                            <head>
                              <meta charset="UTF-8">
                              <title>Autorizacion cancelada</title>
                            </head>
                            <body>
                              <h2>Autorizacion de Google Drive cancelada</h2>
                              <p>No se establecio la conexion.</p>
                              <p>Puedes volver al sistema PPP-UPeU.</p>
                            </body>
                            </html>
                            """);
        }

        googleDriveOAuthService.completarAutorizacion(
                state,
                code
        );

        return ResponseEntity.ok("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <title>Google Drive conectado</title>
                </head>
                <body>
                  <h2>Google Drive autorizado correctamente</h2>
                  <p>La conexion OAuth se completo.</p>
                  <p>Puedes cerrar esta ventana y volver a PPP-UPeU.</p>
                </body>
                </html>
                """);
    }

    // ==========================================
    // 3. CONSULTAR ESTADO DE LA CONEXION
    // ==========================================

    @GetMapping("/estado")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('COORDINADOR_PPP')")
    public Map<String, Boolean> estado() {

        return Map.of(
                "conectado",
                googleDriveOAuthService.estaConectado()
        );
    }
}
