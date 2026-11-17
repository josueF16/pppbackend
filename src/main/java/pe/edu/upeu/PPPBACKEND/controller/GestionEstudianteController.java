
package pe.edu.upeu.PPPBACKEND.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.PPPBACKEND.dto.EstudiantePendienteDTO;
import pe.edu.upeu.PPPBACKEND.service.service.GestionEstudianteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/estudiantes")
@PreAuthorize("hasRole('COORDINADOR_PPP')")
@SecurityRequirement(name = "bearerAuth")
public class GestionEstudianteController {

    private final GestionEstudianteService gestionEstudianteService;

    public GestionEstudianteController(
            GestionEstudianteService gestionEstudianteService) {
        this.gestionEstudianteService = gestionEstudianteService;
    }

    // Listar estudiantes pendientes
    @GetMapping("/pendientes")
    public List<EstudiantePendienteDTO> listarPendientes() {
        return gestionEstudianteService.listarPendientes();
    }

    // Aprobar estudiante
    @PatchMapping("/{id}/aprobar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aprobarEstudiante(@PathVariable Long id) {
        gestionEstudianteService.aprobarEstudiante(id);
    }

    // Rechazar estudiante
    @PatchMapping("/{id}/rechazar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rechazarEstudiante(@PathVariable Long id) {
        gestionEstudianteService.rechazarEstudiante(id);
    }
}
