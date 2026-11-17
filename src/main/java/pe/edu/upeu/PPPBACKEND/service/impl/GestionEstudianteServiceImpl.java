
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.dto.EstudiantePendienteDTO;
import pe.edu.upeu.PPPBACKEND.entity.Usuario;
import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;
import pe.edu.upeu.PPPBACKEND.service.service.GestionEstudianteService;

import java.util.List;

@Service
@PreAuthorize("hasRole('COORDINADOR_PPP')")
public class GestionEstudianteServiceImpl
        implements GestionEstudianteService {

    private final UsuarioRepository usuarioRepository;

    public GestionEstudianteServiceImpl(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // 1. LISTAR ESTUDIANTES PENDIENTES
    @Override
    @Transactional(readOnly = true)
    public List<EstudiantePendienteDTO> listarPendientes() {

        List<Usuario> estudiantes =
                usuarioRepository.findByRolAndEstadoOrderByIdUsuarioAsc(
                        "ESTUDIANTE",
                        "PENDIENTE"
                );

        return estudiantes.stream()
                .map(usuario -> new EstudiantePendienteDTO(
                        usuario.getIdUsuario(),
                        usuario.getNombres(),
                        usuario.getApellidos(),
                        usuario.getDni(),
                        usuario.getCorreo(),
                        usuario.getEstado()
                ))
                .toList();
    }

    // 2. APROBAR ESTUDIANTE
    @Override
    @Transactional
    public void aprobarEstudiante(Long idUsuario) {

        Usuario usuario = obtenerEstudiantePendiente(idUsuario);

        usuario.setEstado("ACTIVO");

        usuarioRepository.saveAndFlush(usuario);
    }

    // 3. RECHAZAR ESTUDIANTE
    @Override
    @Transactional
    public void rechazarEstudiante(Long idUsuario) {

        Usuario usuario = obtenerEstudiantePendiente(idUsuario);

        usuario.setEstado("RECHAZADO");

        usuarioRepository.saveAndFlush(usuario);
    }

    // 4. VALIDACIONES COMPARTIDAS
    private Usuario obtenerEstudiantePendiente(Long idUsuario) {

        Usuario usuario = usuarioRepository
                .findByIdForUpdate(idUsuario)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado"
                        )
                );

        if (!"ESTUDIANTE".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario no tiene el rol ESTUDIANTE"
            );
        }

        if (!"PENDIENTE".equals(usuario.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El estudiante no se encuentra PENDIENTE"
            );
        }

        return usuario;
    }
}
