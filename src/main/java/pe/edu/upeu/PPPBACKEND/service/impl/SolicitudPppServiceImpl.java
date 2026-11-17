
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.EmpresaPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.SolicitudPppService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudPppServiceImpl
        implements SolicitudPppService {

    private final SolicitudPppRepository repository;
    private final EmpresaPppRepository empresaRepository;

    public SolicitudPppServiceImpl(
            SolicitudPppRepository repository,
            EmpresaPppRepository empresaRepository) {
        this.repository = repository;
        this.empresaRepository = empresaRepository;
    }

    private void verificarRol(Long idUsuario, String rol) {
        String rolActual = repository.obtenerRol(idUsuario);

        if (!rol.equalsIgnoreCase(
                rolActual == null ? "" : rolActual)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene permisos para esta operacion"
            );
        }
    }

    @Override
    @Transactional
    public SolicitudPpp registrar(Long idUsuario, Long idEmpresa) {

        verificarRol(idUsuario, "ESTUDIANTE");

        if (idEmpresa == null ||
                !empresaRepository.existsById(idEmpresa)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Empresa no encontrada"
            );
        }

        SolicitudPpp solicitud = new SolicitudPpp();
        solicitud.setIdUsuario(idUsuario);
        solicitud.setIdEmpresa(idEmpresa);
        solicitud.setEstado("PENDIENTE");

        return repository.save(solicitud);
    }

    @Override
    public List<SolicitudPpp> misSolicitudes(Long idUsuario) {
        verificarRol(idUsuario, "ESTUDIANTE");

        return repository.findByIdUsuarioOrderByFechaSolicitudDesc(
                idUsuario
        );
    }

    @Override
    public List<SolicitudPpp> listar(Long idCoordinador) {
        verificarRol(idCoordinador, "COORDINADOR_PPP");
        return repository.findAllByOrderByFechaSolicitudDesc();
    }

    @Override
    public SolicitudPppRepository.DetalleSolicitud detalle(
            Long idCoordinador, Long idSolicitud) {

        verificarRol(idCoordinador, "COORDINADOR_PPP");

        var detalle = repository.obtenerDetalle(idSolicitud);

        if (detalle == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Solicitud no encontrada"
            );
        }

        return detalle;
    }

    @Override
    @Transactional
    public SolicitudPpp validar(
            Long idCoordinador,
            Long idSolicitud,
            String estado,
            String comentario) {

        verificarRol(idCoordinador, "COORDINADOR_PPP");

        if (!"APROBADA".equals(estado) &&
                !"RECHAZADA".equals(estado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado invalido"
            );
        }

        SolicitudPpp solicitud = repository.findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya fue revisada"
            );
        }

        solicitud.setEstado(estado);
        solicitud.setComentario(comentario);
        solicitud.setFechaRevision(LocalDateTime.now());

        return repository.save(solicitud);
    }
}
