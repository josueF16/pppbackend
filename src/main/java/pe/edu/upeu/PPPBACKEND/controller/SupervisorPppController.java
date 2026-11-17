
package pe.edu.upeu.PPPBACKEND.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.PPPBACKEND.entity.SupervisorPpp;
import pe.edu.upeu.PPPBACKEND.entity.AsignacionSupervisorPpp;
import pe.edu.upeu.PPPBACKEND.service.impl.SupervisorPppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supervisores")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('COORDINADOR_PPP')")
public class SupervisorPppController {

    private final SupervisorPppService supervisorService;

    public SupervisorPppController(
            SupervisorPppService supervisorService) {

        this.supervisorService = supervisorService;
    }

    // ==========================================
    // RF75 - LISTAR SUPERVISORES
    // ==========================================

    @GetMapping
    public List<SupervisorPpp> listar() {
        return supervisorService.listar();
    }

    // ==========================================
    // RF76 - VER DATOS DE UN SUPERVISOR
    // ==========================================

    @GetMapping("/{id}")
    public SupervisorPpp obtener(@PathVariable Long id) {
        return supervisorService.obtener(id);
    }

    // ==========================================
    // RF78 - REGISTRAR SUPERVISOR
    // ==========================================

    @PostMapping
    public ResponseEntity<SupervisorPpp> registrar(
            @RequestBody NuevoSupervisorRequest datos) {

        SupervisorPpp nuevo = supervisorService.registrar(
                datos.nombres(),
                datos.apellidos(),
                datos.correo(),
                datos.telefono(),
                datos.tipo()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevo);
    }

    // ==========================================
    // RF79 - ASIGNAR SUPERVISORES
    // ==========================================

    @PutMapping("/asignaciones")
    public AsignacionSupervisorPpp asignar(
            @RequestBody AsignacionRequest datos) {

        return supervisorService.asignar(
                datos.idSolicitud(),
                datos.idSupervisorAcademico(),
                datos.idSupervisorEmpresarial()
        );
    }

    // ==========================================
    // CONSULTAR ASIGNACION DE UNA SOLICITUD
    // ==========================================

    @GetMapping("/asignaciones/solicitud/{idSolicitud}")
    public AsignacionSupervisorPpp consultarAsignacion(
            @PathVariable Long idSolicitud) {

        return supervisorService.obtenerAsignacion(idSolicitud);
    }

    // ==========================================
    // DATOS DE ENTRADA
    // ==========================================

    public record NuevoSupervisorRequest(
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String tipo
    ) {}

    public record AsignacionRequest(
            Long idSolicitud,
            Long idSupervisorAcademico,
            Long idSupervisorEmpresarial
    ) {}
}
