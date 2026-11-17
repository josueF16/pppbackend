
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.dto.EstudiantePendienteDTO;

import java.util.List;

public interface GestionEstudianteService {

    // Obtener estudiantes pendientes de aprobacion
    List<EstudiantePendienteDTO> listarPendientes();

    // Aprobar un estudiante pendiente
    void aprobarEstudiante(Long idUsuario);

    // Rechazar un estudiante pendiente
    void rechazarEstudiante(Long idUsuario);
}
