
package pe.edu.upeu.PPPBACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstudiantePendienteDTO {

    private Long idUsuario;

    private String nombres;

    private String apellidos;

    private String dni;

    private String correo;

    private String estado;
}
