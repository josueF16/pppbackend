
package pe.edu.upeu.PPPBACKEND.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ASIGNACION_SUPERVISOR_PPP")
@Getter
@Setter
@NoArgsConstructor
public class AsignacionSupervisorPpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASIGNACION")
    private Long idAsignacion;

    @Column(name = "ID_SOLICITUD", nullable = false, unique = true)
    private Long idSolicitud;

    @Column(name = "ID_SUPERVISOR_ACADEMICO")
    private Long idSupervisorAcademico;

    @Column(name = "ID_SUPERVISOR_EMPRESARIAL")
    private Long idSupervisorEmpresarial;

    @Column(
            name = "FECHA_ASIGNACION",
            insertable = false,
            updatable = false
    )
    private LocalDateTime fechaAsignacion;
}
