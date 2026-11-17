
package pe.edu.upeu.PPPBACKEND.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;
import pe.edu.upeu.PPPBACKEND.service.impl.GoogleDriveUploadService;

@RestController
@RequestMapping("/api/v1/google/drive")
@SecurityRequirement(name = "bearerAuth")
public class GoogleDriveUploadController {

    private final GoogleDriveUploadService googleDriveUploadService;

    public GoogleDriveUploadController(
            GoogleDriveUploadService googleDriveUploadService) {

        this.googleDriveUploadService = googleDriveUploadService;
    }

    // ==========================================
    // SUBIR CARTA DE ACEPTACION O CONVENIO
    // ==========================================

    @PostMapping(
            value = "/solicitud/{idSolicitud}/subir",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<DocumentoCartaPpp> subirDocumento(
            @PathVariable Long idSolicitud,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestPart("archivo") MultipartFile archivo,
            @AuthenticationPrincipal Jwt jwt) {

        // ======================================
        // 1. VALIDAR AUTENTICACION
        // ======================================

        if (jwt == null || jwt.getSubject() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesion"
            );
        }

        // ======================================
        // 2. IDENTIFICAR ESTUDIANTE
        // ======================================

        Long idEstudiante;

        try {

            idEstudiante = Long.valueOf(
                    jwt.getSubject()
            );

        } catch (NumberFormatException ex) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificador de usuario invalido"
            );
        }

        // ======================================
        // 3. SUBIR ARCHIVO Y REGISTRAR EN ORACLE
        // ======================================

        DocumentoCartaPpp documento =
                googleDriveUploadService.subirDocumento(
                        idEstudiante,
                        idSolicitud,
                        tipoDocumento,
                        archivo
                );

        // ======================================
        // 4. DEVOLVER DOCUMENTO REGISTRADO
        // ======================================

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documento);
    }
}
