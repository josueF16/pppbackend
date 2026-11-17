
package pe.edu.upeu.PPPBACKEND.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.CartaPresentacionPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.service.service.CartaPresentacionPppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cartas")
public class CartaPresentacionPppController {

    private final CartaPresentacionPppService service;

    public CartaPresentacionPppController(
            CartaPresentacionPppService service) {
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

    public record CrearCartaRequest(
            @NotBlank String modelo
    ) {}

    public record GuardarCartaRequest(
            @NotBlank String contenido
    ) {}

    // RF47 - RF49: Solicitudes aprobadas
    @GetMapping("/solicitudes-aprobadas")
    public List<SolicitudPpp> solicitudesAprobadas(
            Authentication auth) {

        return service.solicitudesAprobadas(
                usuarioActual(auth)
        );
    }

    // RF50 - RF52: Crear carta con datos del estudiante
    @PostMapping("/solicitud/{idSolicitud}")
    @ResponseStatus(HttpStatus.CREATED)
    public CartaPresentacionPpp crearBorrador(
            @PathVariable Long idSolicitud,
            @Valid @RequestBody CrearCartaRequest request,
            Authentication auth) {

        return service.crearBorrador(
                usuarioActual(auth),
                idSolicitud,
                request.modelo()
        );
    }

    // RF53: Guardar cambios del borrador
    @PutMapping("/{idCarta}")
    public CartaPresentacionPpp guardar(
            @PathVariable Long idCarta,
            @Valid @RequestBody GuardarCartaRequest request,
            Authentication auth) {

        return service.guardarBorrador(
                usuarioActual(auth),
                idCarta,
                request.contenido()
        );
    }

    // RF54: Consultar contenido para vista previa
    @GetMapping("/{idCarta}")
    public CartaPresentacionPpp obtener(
            @PathVariable Long idCarta,
            Authentication auth) {

        return service.obtener(
                usuarioActual(auth),
                idCarta
        );
    }

    // RF55 y RF57: Enviar y registrar fecha de envio
    @PostMapping("/{idCarta}/enviar")
    public CartaPresentacionPpp enviar(
            @PathVariable Long idCarta,
            Authentication auth) {

        return service.enviar(
                usuarioActual(auth),
                idCarta
        );
    }

    // RF56: Cancelar antes del envio
    @PostMapping("/{idCarta}/cancelar")
    public CartaPresentacionPpp cancelar(
            @PathVariable Long idCarta,
            Authentication auth) {

        return service.cancelarEnvio(
                usuarioActual(auth),
                idCarta
        );
    }

    // Cartas enviadas al estudiante
    @GetMapping("/recibidas")
    public List<CartaPresentacionPpp> recibidas(
            Authentication auth) {

        return service.cartasRecibidas(
                usuarioActual(auth)
        );
    }
}
