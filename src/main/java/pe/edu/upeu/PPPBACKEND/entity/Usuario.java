package pe.edu.upeu.PPPBACKEND.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "NOMBRES", nullable = false, length = 80)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "DNI", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "CORREO", nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "CLAVE", nullable = false, length = 255)
    private String clave;

    @Column(name = "ROL", nullable = false, length = 30)
    private String rol;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado = "ACTIVO";
}