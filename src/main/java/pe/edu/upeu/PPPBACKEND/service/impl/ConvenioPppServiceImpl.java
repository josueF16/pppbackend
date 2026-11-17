
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.ConvenioPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.ConvenioPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.ConvenioPppService;

import java.time.LocalDate;

@Service
public class ConvenioPppServiceImpl
        implements ConvenioPppService {

    private final ConvenioPppRepository repository;
    private final SolicitudPppRepository solicitudRepository;

    public ConvenioPppServiceImpl(
            ConvenioPppRepository repository,
            SolicitudPppRepository solicitudRepository) {

        this.repository = repository;
        this.solicitudRepository = solicitudRepository;
    }

    private void verificarSolicitud(
            Long idEstudiante,
            Long idSolicitud) {

        String rol = solicitudRepository.obtenerRol(idEstudiante);

        if (!"ESTUDIANTE".equals(rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los estudiantes pueden registrar convenios"
            );
        }

        SolicitudPpp solicitud = solicitudRepository
                .findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        if (!solicitud.getIdUsuario().equals(idEstudiante)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta solicitud no pertenece al estudiante"
            );
        }

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud no esta aprobada"
            );
        }
    }

    @Override
    @Transactional
    public ConvenioPpp registrar(
            Long idEstudiante,
            Long idSolicitud,
            String cargo,
            String horario,
            String pago,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        verificarSolicitud(idEstudiante, idSolicitud);

        if (cargo == null || cargo.isBlank()
                || cargo.length() > 150
                || (horario != null && horario.length() > 200)
                || (pago != null && pago.length() > 150)
                || fechaInicio == null
                || fechaFin == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos del convenio incompletos o invalidos"
            );
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha final no puede ser anterior a la inicial"
            );
        }

        if (repository.findByIdSolicitud(idSolicitud).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya tiene un convenio"
            );
        }

        ConvenioPpp convenio = new ConvenioPpp();

        convenio.setIdSolicitud(idSolicitud);
        convenio.setCargo(cargo);
        convenio.setHorario(horario);
        convenio.setPago(pago);
        convenio.setFechaInicio(fechaInicio);
        convenio.setFechaFin(fechaFin);

        return repository.save(convenio);
    }

    @Override
    public ConvenioPpp obtener(
            Long idEstudiante,
            Long idSolicitud) {

        verificarSolicitud(idEstudiante, idSolicitud);

        return repository.findByIdSolicitud(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Convenio no encontrado"
                ));
    }
}
