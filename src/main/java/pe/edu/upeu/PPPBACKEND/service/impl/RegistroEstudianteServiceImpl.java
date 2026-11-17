
package pe.edu.upeu.PPPBACKEND.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.dto.RegistroEstudianteRequestDTO;
import pe.edu.upeu.PPPBACKEND.entity.Usuario;
import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;
import pe.edu.upeu.PPPBACKEND.service.service.RegistroEstudianteService;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistroEstudianteServiceImpl
        implements RegistroEstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registrar(RegistroEstudianteRequestDTO request) {

        // 1. Normalizar la información recibida
        String nombres = request.nombres().trim();
        String apellidos = request.apellidos().trim();
        String dni = request.dni().trim();

        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        // 2. Comprobar que el correo no esté registrado
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya se encuentra registrado"
            );
        }

        // 3. Comprobar que el DNI no esté registrado
        if (usuarioRepository.existsByDni(dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El DNI ya se encuentra registrado"
            );
        }

        // 4. Crear la entidad Usuario
        Usuario usuario = new Usuario();

        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setDni(dni);
        usuario.setCorreo(correo);

        // 5. Cifrar la contraseña con BCrypt
        usuario.setClave(
                passwordEncoder.encode(request.clave())
        );

        // 6. Asignar el rol desde el backend
        usuario.setRol("ESTUDIANTE");

        // 7. No activar la cuenta automáticamente
        usuario.setEstado("PENDIENTE");

        // 8. Guardar en Oracle
        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo registrar: correo o DNI duplicado",
                    ex
            );
        }
    }
}
