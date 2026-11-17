
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.SupervisorPpp;
import pe.edu.upeu.PPPBACKEND.entity.AsignacionSupervisorPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;

import pe.edu.upeu.PPPBACKEND.repository.SupervisorPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.AsignacionSupervisorPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;

import java.util.List;
import java.util.Locale;

@Service
public class SupervisorPppService {

    private final SupervisorPppRepository supervisorRepository;
    private final AsignacionSupervisorPppRepository asignacionRepository;
    private final SolicitudPppRepository solicitudRepository;

    public SupervisorPppService(
            SupervisorPppRepository supervisorRepository,
            AsignacionSupervisorPppRepository asignacionRepository,
            SolicitudPppRepository solicitudRepository) {

        this.supervisorRepository = supervisorRepository;
        this.asignacionRepository = asignacionRepository;
        this.solicitudRepository = solicitudRepository;
    }

    // ==========================================
    // RF75 - LISTAR SUPERVISORES
    // ==========================================

    public List<SupervisorPpp> listar() {
        return supervisorRepository.findAll();
    }

    // ==========================================
    // RF76 - CONSULTAR DATOS DE SUPERVISOR
    // ==========================================

    public SupervisorPpp obtener(Long id) {

        return supervisorRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Supervisor no encontrado"
                        )
                );
    }

    // ==========================================
    // RF78 - REGISTRAR SUPERVISOR
    // ==========================================

    public SupervisorPpp registrar(
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String tipo) {

        nombres = obligatorio(nombres, "Nombres", 80);
        apellidos = obligatorio(apellidos, "Apellidos", 80);
        correo = obligatorio(correo, "Correo", 120)
                .toLowerCase(Locale.ROOT);
        tipo = obligatorio(tipo, "Tipo", 20)
                .toUpperCase(Locale.ROOT);

        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Correo electronico invalido"
            );
        }

        if (!tipo.equals("ACADEMICO")
                && !tipo.equals("EMPRESARIAL")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo debe ser ACADEMICO o EMPRESARIAL"
            );
        }

        if (telefono != null) {
            telefono = telefono.trim();

            if (telefono.length() > 25) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Telefono demasiado largo"
                );
            }
        }

        if (supervisorRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un supervisor con ese correo"
            );
        }

        SupervisorPpp supervisor = new SupervisorPpp();

        supervisor.setNombres(nombres);
        supervisor.setApellidos(apellidos);
        supervisor.setCorreo(correo);
        supervisor.setTelefono(telefono);
        supervisor.setTipo(tipo);
        supervisor.setEstado("ACTIVO");

        try {
            return supervisorRepository.saveAndFlush(supervisor);

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo registrar: datos duplicados o invalidos"
            );
        }
    }

    // ==========================================
    // RF79 - ASIGNAR SUPERVISORES
    // ==========================================

    @Transactional
    public AsignacionSupervisorPpp asignar(
            Long idSolicitud,
            Long idAcademico,
            Long idEmpresarial) {

        if (idSolicitud == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indica una solicitud"
            );
        }

        if (idAcademico == null && idEmpresarial == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona al menos un supervisor"
            );
        }

        // La solicitud debe existir.
        SolicitudPpp solicitud = solicitudRepository
                .findById(idSolicitud)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitud PPP no encontrada"
                        )
                );

        // Solo solicitudes aprobadas.
        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud PPP debe estar aprobada"
            );
        }

        // Validar los tipos de supervisores.
        if (idAcademico != null) {
            validarSupervisor(idAcademico, "ACADEMICO");
        }

        if (idEmpresarial != null) {
            validarSupervisor(idEmpresarial, "EMPRESARIAL");
        }

        // Si la solicitud ya tiene una asignacion,
        // actualizarla sin perder los otros datos.
        AsignacionSupervisorPpp asignacion =
                asignacionRepository
                        .findByIdSolicitud(idSolicitud)
                        .orElseGet(() -> {

                            AsignacionSupervisorPpp nueva =
                                    new AsignacionSupervisorPpp();

                            nueva.setIdSolicitud(idSolicitud);

                            return nueva;
                        });

        if (idAcademico != null) {
            asignacion.setIdSupervisorAcademico(idAcademico);
        }

        if (idEmpresarial != null) {
            asignacion.setIdSupervisorEmpresarial(idEmpresarial);
        }

        try {
            return asignacionRepository.saveAndFlush(asignacion);

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar la asignacion"
            );
        }
    }

    // ==========================================
    // CONSULTAR ASIGNACION DE UNA SOLICITUD
    // ==========================================

    public AsignacionSupervisorPpp obtenerAsignacion(
            Long idSolicitud) {

        return asignacionRepository
                .findByIdSolicitud(idSolicitud)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "La solicitud no tiene supervisores asignados"
                        )
                );
    }

    // ==========================================
    // VALIDAR SUPERVISOR
    // ==========================================

    private void validarSupervisor(Long id, String tipoEsperado) {

        SupervisorPpp supervisor = obtener(id);

        if (!"ACTIVO".equals(supervisor.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El supervisor esta inactivo"
            );
        }

        if (!tipoEsperado.equals(supervisor.getTipo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El supervisor no corresponde al tipo solicitado"
            );
        }
    }

    // ==========================================
    // VALIDAR TEXTO OBLIGATORIO
    // ==========================================

    private String obligatorio(
            String valor,
            String campo,
            int longitudMaxima) {

        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    campo + " es obligatorio"
            );
        }

        String limpio = valor.trim();

        if (limpio.length() > longitudMaxima) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    campo + " supera la longitud permitida"
            );
        }

        return limpio;
    }
}
