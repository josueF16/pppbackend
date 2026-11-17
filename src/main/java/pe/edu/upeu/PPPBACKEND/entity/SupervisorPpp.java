
package pe.edu.upeu.PPPBACKEND.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SUPERVISOR_PPP")
@Getter
@Setter
@NoArgsConstructor
public class SupervisorPpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SUPERVISOR")
    private Long idSupervisor;

    @Column(name = "NOMBRES", nullable = false, length = 80)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "CORREO", nullable = false, length = 120, unique = true)
    private String correo;

    @Column(name = "TELEFONO", length = 25)
    private String telefono;

    @Column(name = "TIPO", nullable = false, length = 20)
    private String tipo;

    @Column(name = "ESTADO", nullable = false, length = 15)
    private String estado = "ACTIVO";
}
