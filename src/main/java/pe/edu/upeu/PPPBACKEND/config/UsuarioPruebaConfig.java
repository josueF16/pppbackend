package pe.edu.upeu.PPPBACKEND.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.PPPBACKEND.entity.Usuario;
import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;

@Configuration
@ConditionalOnProperty(
        name = "ppp.seed.enabled",
        havingValue = "true"
)
public class UsuarioPruebaConfig {

    @Bean
    public CommandLineRunner crearUsuarioPrueba(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${PPP_TEST_PASSWORD}") String clave) {

        return args -> {
            String correo = "estudiante@example.com";

            if (repository.existsByCorreo(correo)) {
                System.out.println(
                        "Usuario de prueba existente; no se modificó."
                );
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setNombres("Estudiante");
            usuario.setApellidos("De Prueba");
            usuario.setDni("00000001");
            usuario.setCorreo(correo);
            usuario.setClave(passwordEncoder.encode(clave));
            usuario.setRol("ESTUDIANTE");
            usuario.setEstado("ACTIVO");

            repository.save(usuario);

            System.out.println(
                    "Usuario de prueba creado correctamente."
            );
        };
    }
}