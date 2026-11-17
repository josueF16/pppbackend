
package pe.edu.upeu.PPPBACKEND.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoCartaPpp;
import pe.edu.upeu.PPPBACKEND.service.service.DocumentoCartaPppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos-cartas")
public class DocumentoCartaPppController {

    private final DocumentoCartaPppService service;

    public DocumentoCartaPppController(
            DocumentoCartaPppService service) {
        this.service = service;
    }

    private Long usuarioActual(Authentication auth) {

        if (auth == null ||
                !auth.isAuthenticated() ||
                !auth.getName().matches("\\d+")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        return Long.valueOf(auth.getName());
    }

    public record DocumentoCartaRequest(
            @NotBlank String tipoDocumento,
            @NotBlank String nombreArchivo,
            @NotBlank String driveFileId,
            @NotBlank String urlDocumento
    ) {}

    // Registrar carta de aceptacion o convenio
    @PostMapping("/solicitud/{idSolicitud}")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoCartaPpp registrar(
            @PathVariable Long idSolicitud,
            @Valid @RequestBody DocumentoCartaRequest request,
            Authentication auth) {

        return service.registrar(
                usuarioActual(auth),
                idSolicitud,
                request.tipoDocumento(),
                request.nombreArchivo(),
                request.driveFileId(),
                request.urlDocumento()
        );
    }

    // Listar documentos de una solicitud
    @GetMapping("/solicitud/{idSolicitud}")
    public List<DocumentoCartaPpp> listar(
            @PathVariable Long idSolicitud,
            Authentication auth) {

        return service.listar(
                usuarioActual(auth),
                idSolicitud
        );
    }

    // Consultar un documento
    @GetMapping("/{idDocumento}")
    public DocumentoCartaPpp obtener(
            @PathVariable Long idDocumento,
            Authentication auth) {

        return service.obtener(
                usuarioActual(auth),
                idDocumento
        );
    }
}
