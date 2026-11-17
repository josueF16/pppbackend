
package pe.edu.upeu.PPPBACKEND.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.DocumentoDrivePpp;
import pe.edu.upeu.PPPBACKEND.service.service.DocumentoDrivePppService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documentos-drive")
public class DocumentoDrivePppController {

    private final DocumentoDrivePppService service;

    public DocumentoDrivePppController(
            DocumentoDrivePppService service) {
        this.service = service;
    }

    private Long usuarioActual(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || !auth.getName().matches("\\d+")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        return Long.valueOf(auth.getName());
    }

    public record DocumentoDriveRequest(
            @NotBlank String driveFileId,
            @NotBlank String urlDocumento,
            @NotBlank String urlVistaPrevia
    ) {}

    @PostMapping("/solicitud/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoDrivePpp guardar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoDriveRequest request,
            Authentication auth) {

        return service.guardar(
                usuarioActual(auth),
                id,
                request.driveFileId(),
                request.urlDocumento(),
                request.urlVistaPrevia()
        );
    }

    @GetMapping("/solicitud/{id}")
    public DocumentoDrivePpp consultar(
            @PathVariable Long id,
            Authentication auth) {

        return service.consultar(usuarioActual(auth), id);
    }

    @GetMapping("/solicitud/{id}/vista-previa")
    public Map<String, String> vistaPrevia(
            @PathVariable Long id,
            Authentication auth) {

        DocumentoDrivePpp documento = service.consultar(
                usuarioActual(auth), id
        );

        return Map.of(
                "urlVistaPrevia",
                documento.getUrlVistaPrevia()
        );
    }
}
