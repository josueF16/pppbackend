
package pe.edu.upeu.PPPBACKEND.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.SolicitudPppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudPppController {

    private final SolicitudPppService service;

    public SolicitudPppController(SolicitudPppService service) {
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

    public record CrearSolicitudRequest(
            @NotNull Long idEmpresa
    ) {}

    public record ValidarSolicitudRequest(
            String estado,
            String comentario
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudPpp registrar(
            @Valid @RequestBody CrearSolicitudRequest request,
            Authentication auth) {

        return service.registrar(
                usuarioActual(auth),
                request.idEmpresa()
        );
    }

    @GetMapping("/mias")
    public List<SolicitudPpp> misSolicitudes(
            Authentication auth) {

        return service.misSolicitudes(usuarioActual(auth));
    }

    @GetMapping
    public List<SolicitudPpp> listar(Authentication auth) {
        return service.listar(usuarioActual(auth));
    }

    @GetMapping("/{id}")
    public SolicitudPppRepository.DetalleSolicitud detalle(
            @PathVariable Long id,
            Authentication auth) {

        return service.detalle(usuarioActual(auth), id);
    }

    @PatchMapping("/{id}/validar")
    public SolicitudPpp validar(
            @PathVariable Long id,
            @RequestBody ValidarSolicitudRequest request,
            Authentication auth) {

        return service.validar(
                usuarioActual(auth),
                id,
                request.estado(),
                request.comentario()
        );
    }
}
