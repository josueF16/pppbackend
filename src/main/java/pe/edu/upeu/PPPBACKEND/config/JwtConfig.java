
package pe.edu.upeu.PPPBACKEND.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${JWT_SECRET}") String secret) {

        byte[] bytes = Base64.getDecoder().decode(secret);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET debe contener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {

        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            UsuarioRepository usuarioRepository) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Validar el estado actual del usuario en Oracle.
        OAuth2TokenValidator<Jwt> estadoUsuarioValidator = jwt -> {

            String subject = jwt.getSubject();

            if (subject == null || subject.isBlank()) {
                return tokenNoAutorizado();
            }

            try {
                // El subject contiene el ID_USUARIO.
                Long idUsuario = Long.valueOf(subject);

                // Consultar el estado actual del usuario.
                boolean usuarioActivo = usuarioRepository
                        .findById(idUsuario)
                        .map(usuario ->
                                "ACTIVO".equals(usuario.getEstado())
                        )
                        .orElse(false);

                if (usuarioActivo) {
                    return OAuth2TokenValidatorResult.success();
                }

            } catch (NumberFormatException ex) {
                // Subject invalido: rechazar el JWT.
            }

            return tokenNoAutorizado();
        };

        // Mantener los validadores originales
        // y agregar la verificacion del estado.
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer("PPPBACKEND"),
                        estadoUsuarioValidator
                )
        );

        return decoder;
    }

    private OAuth2TokenValidatorResult tokenNoAutorizado() {

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "Token no autorizado",
                null
        );

        return OAuth2TokenValidatorResult.failure(error);
    }
}
