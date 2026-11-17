package pe.edu.upeu.PPPBACKEND.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String tipoToken;
    private long expiresIn;
    private UsuarioResponseDTO usuario;
}