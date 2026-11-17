
package pe.edu.upeu.PPPBACKEND.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUD_PPP")
public class SolicitudPpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SOLICITUD")
    private Long idSolicitud;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long idUsuario;

    @Column(name = "ID_EMPRESA", nullable = false)
    private Long idEmpresa;

    @Column(name = "FECHA_SOLICITUD", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "ESTADO", nullable = false)
    private String estado;

    @Column(name = "COMENTARIO")
    private String comentario;

    @Column(name = "FECHA_REVISION")
    private LocalDateTime fechaRevision;

    @PrePersist
    public void antesDeGuardar() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long v) { this.idSolicitud = v; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long v) { this.idUsuario = v; }

    public Long getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Long v) { this.idEmpresa = v; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime v) { this.fechaSolicitud = v; }

    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }

    public String getComentario() { return comentario; }
    public void setComentario(String v) { this.comentario = v; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime v) { this.fechaRevision = v; }
}
