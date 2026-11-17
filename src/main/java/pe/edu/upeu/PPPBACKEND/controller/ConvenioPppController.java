
package pe.edu.upeu.PPPBACKEND.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.ConvenioPpp;
import pe.edu.upeu.PPPBACKEND.service.service.ConvenioPppService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/convenios")
public class ConvenioPppController {

    private final ConvenioPppService service;

    public ConvenioPppController(
            ConvenioPppService service) {
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

    public record ConvenioRequest(
            @NotBlank String cargo,
            String horario,
            String pago,
            @NotNull LocalDate fechaInicio,
            @NotNull LocalDate fechaFin
    ) {}

    // Registrar datos del convenio
    @PostMapping("/solicitud/{idSolicitud}")
    @ResponseStatus(HttpStatus.CREATED)
    public ConvenioPpp registrar(
            @PathVariable Long idSolicitud,
            @Valid @RequestBody ConvenioRequest request,
            Authentication auth) {

        return service.registrar(
                usuarioActual(auth),
                idSolicitud,
                request.cargo(),
                request.horario(),
                request.pago(),
                request.fechaInicio(),
                request.fechaFin()
        );
    }

    // Consultar convenio registrado
    @GetMapping("/solicitud/{idSolicitud}")
    public ConvenioPpp obtener(
            @PathVariable Long idSolicitud,
            Authentication auth) {

        return service.obtener(
                usuarioActual(auth),
                idSolicitud
        );
    }
}
