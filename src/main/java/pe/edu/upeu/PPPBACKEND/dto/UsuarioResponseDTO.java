package pe.edu.upeu.PPPBACKEND.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioResponseDTO {

    private Long idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private String estado;
}