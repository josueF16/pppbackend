package pe.edu.upeu.PPPBACKEND.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PPPBACKEND.dto.LoginRequestDTO;
import pe.edu.upeu.PPPBACKEND.dto.LoginResponseDTO;
import pe.edu.upeu.PPPBACKEND.dto.UsuarioResponseDTO;
import pe.edu.upeu.PPPBACKEND.entity.Usuario;
import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;
import pe.edu.upeu.PPPBACKEND.service.service.AuthService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtEncoder jwtEncoder;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                request.getCorreo().trim(),
                                request.getClave()
                        )
                );

        Usuario usuario = usuarioRepository
                .findByCorreo(authentication.getName())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Credenciales incorrectas"
                        )
                );

        if (!"ACTIVO".equals(usuario.getEstado())) {
            throw new BadCredentialsException(
                    "Credenciales incorrectas"
            );
        }

        Instant ahora = Instant.now();
        long duracionSegundos = 900;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("PPPBACKEND")
                .subject(usuario.getIdUsuario().toString())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(duracionSegundos))
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of(usuario.getRol()))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        UsuarioResponseDTO datosUsuario = new UsuarioResponseDTO();
        datosUsuario.setIdUsuario(usuario.getIdUsuario());
        datosUsuario.setNombres(usuario.getNombres());
        datosUsuario.setApellidos(usuario.getApellidos());
        datosUsuario.setCorreo(usuario.getCorreo());
        datosUsuario.setRol(usuario.getRol());
        datosUsuario.setEstado(usuario.getEstado());

        LoginResponseDTO respuesta = new LoginResponseDTO();
        respuesta.setToken(token);
        respuesta.setTipoToken("Bearer");
        respuesta.setExpiresIn(duracionSegundos);
        respuesta.setUsuario(datosUsuario);

        return respuesta;
    }
}