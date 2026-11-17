package pe.edu.upeu.PPPBACKEND.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PPPBACKEND.entity.Usuario;
import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Credenciales incorrectas"
                        )
                );

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getClave())
                .authorities("ROLE_" + usuario.getRol())
                .disabled(!"ACTIVO".equals(usuario.getEstado()))
                .build();
    }
}