
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.CartaPresentacionPpp;
import pe.edu.upeu.PPPBACKEND.entity.SolicitudPpp;
import pe.edu.upeu.PPPBACKEND.repository.CartaPresentacionPppRepository;
import pe.edu.upeu.PPPBACKEND.repository.SolicitudPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.CartaPresentacionPppService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CartaPresentacionPppServiceImpl
        implements CartaPresentacionPppService {

    private final CartaPresentacionPppRepository repository;
    private final SolicitudPppRepository solicitudRepository;
    private final JdbcTemplate jdbcTemplate;

    public CartaPresentacionPppServiceImpl(
            CartaPresentacionPppRepository repository,
            SolicitudPppRepository solicitudRepository,
            JdbcTemplate jdbcTemplate) {

        this.repository = repository;
        this.solicitudRepository = solicitudRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void verificarRol(Long idUsuario, String rol) {

        String rolActual = repository.obtenerRol(idUsuario);

        if (!rol.equals(rolActual)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene permisos para esta operacion"
            );
        }
    }

    private SolicitudPpp obtenerSolicitud(Long idSolicitud) {

        return solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));
    }

    private CartaPresentacionPpp buscarCarta(Long idCarta) {

        return repository.findById(idCarta)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Carta no encontrada"
                ));
    }

    @Override
    public List<SolicitudPpp> solicitudesAprobadas(Long idSecretaria) {

        verificarRol(idSecretaria, "SECRETARIA");

        return solicitudRepository
                .findAllByOrderByFechaSolicitudDesc()
                .stream()
                .filter(s -> "APROBADA".equals(s.getEstado()))
                .toList();
    }

    @Override
    @Transactional
    public CartaPresentacionPpp crearBorrador(
            Long idSecretaria,
            Long idSolicitud,
            String modelo) {

        verificarRol(idSecretaria, "SECRETARIA");

        SolicitudPpp solicitud = obtenerSolicitud(idSolicitud);

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud no esta aprobada"
            );
        }

        if (modelo == null || modelo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe proporcionar el modelo de carta"
            );
        }

        if (!repository
                .findByIdSolicitudOrderByFechaCreacionDesc(idSolicitud)
                .isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya tiene una carta"
            );
        }

        Map<String, Object> datos = jdbcTemplate.queryForMap("""
                SELECT
                    u.NOMBRES,
                    u.APELLIDOS,
                    u.DNI,
                    u.CORREO,
                    e.NOMBRE AS EMPRESA
                FROM SOLICITUD_PPP s
                JOIN USUARIO u
                    ON u.ID_USUARIO = s.ID_USUARIO
                JOIN EMPRESA_PPP e
                    ON e.ID_EMPRESA = s.ID_EMPRESA
                WHERE s.ID_SOLICITUD = ?
                """, idSolicitud);

        String contenido = modelo
                .replace("{{NOMBRES}}",
                        String.valueOf(datos.get("NOMBRES")))
                .replace("{{APELLIDOS}}",
                        String.valueOf(datos.get("APELLIDOS")))
                .replace("{{DNI}}",
                        String.valueOf(datos.get("DNI")).trim())
                .replace("{{CORREO}}",
                        String.valueOf(datos.get("CORREO")))
                .replace("{{EMPRESA}}",
                        String.valueOf(datos.get("EMPRESA")))
                .replace("{{FECHA}}",
                        LocalDate.now().toString());

        CartaPresentacionPpp carta =
                new CartaPresentacionPpp();

        carta.setIdSolicitud(idSolicitud);
        carta.setContenido(contenido);
        carta.setEstado("BORRADOR");

        return repository.save(carta);
    }

    @Override
    @Transactional
    public CartaPresentacionPpp guardarBorrador(
            Long idSecretaria,
            Long idCarta,
            String contenido) {

        verificarRol(idSecretaria, "SECRETARIA");

        CartaPresentacionPpp carta = buscarCarta(idCarta);

        if (!"BORRADOR".equals(carta.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede editar un borrador"
            );
        }

        if (contenido == null || contenido.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El contenido no puede estar vacio"
            );
        }

        carta.setContenido(contenido);

        return repository.save(carta);
    }

    @Override
    public CartaPresentacionPpp obtener(
            Long idSecretaria,
            Long idCarta) {

        verificarRol(idSecretaria, "SECRETARIA");

        return buscarCarta(idCarta);
    }

    @Override
    @Transactional
    public CartaPresentacionPpp enviar(
            Long idSecretaria,
            Long idCarta) {

        verificarRol(idSecretaria, "SECRETARIA");

        CartaPresentacionPpp carta = buscarCarta(idCarta);

        if (!"BORRADOR".equals(carta.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La carta no esta en borrador"
            );
        }

        if (carta.getContenido() == null ||
                carta.getContenido().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La carta no tiene contenido"
            );
        }

        carta.setEstado("ENVIADA");
        carta.setFechaEnvio(LocalDateTime.now());

        return repository.save(carta);
    }

    @Override
    @Transactional
    public CartaPresentacionPpp cancelarEnvio(
            Long idSecretaria,
            Long idCarta) {

        verificarRol(idSecretaria, "SECRETARIA");

        CartaPresentacionPpp carta = buscarCarta(idCarta);

        if (!"BORRADOR".equals(carta.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede cancelar antes del envio"
            );
        }

        carta.setEstado("CANCELADA");

        return repository.save(carta);
    }

    @Override
    public List<CartaPresentacionPpp> cartasRecibidas(
            Long idEstudiante) {

        verificarRol(idEstudiante, "ESTUDIANTE");

        return solicitudRepository
                .findByIdUsuarioOrderByFechaSolicitudDesc(idEstudiante)
                .stream()
                .flatMap(s -> repository
                        .findByIdSolicitudOrderByFechaCreacionDesc(
                                s.getIdSolicitud())
                        .stream())
                .filter(c -> "ENVIADA".equals(c.getEstado()))
                .toList();
    }
}
