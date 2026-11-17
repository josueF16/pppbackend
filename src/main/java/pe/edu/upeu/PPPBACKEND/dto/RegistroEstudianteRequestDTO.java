
package pe.edu.upeu.PPPBACKEND.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroEstudianteRequestDTO(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, max = 80)
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 80)
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "[0-9]{8}",
                message = "El DNI debe contener 8 digitos"
        )
        String dni,

        @NotBlank(message = "El correo institucional es obligatorio")
        @Email(message = "Correo invalido")
        @Size(max = 100)
        @Pattern(
                regexp = "(?i)^[a-z0-9._%+\\-]+@upeu\\.edu\\.pe$",
                message = "Debe utilizar un correo @upeu.edu.pe"
        )
        String correo,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(
                min = 12,
                max = 72,
                message = "La contrasena debe contener entre 12 y 72 caracteres"
        )
        String clave

) {}
