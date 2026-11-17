
package pe.edu.upeu.PPPBACKEND.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DOCUMENTO_DRIVE_PPP")
public class DocumentoDrivePpp {

    @Id
    @Column(name = "ID_SOLICITUD")
    private Long idSolicitud;

    @Column(name = "DRIVE_FILE_ID", nullable = false)
    private String driveFileId;

    @Column(name = "URL_DOCUMENTO", nullable = false)
    private String urlDocumento;

    @Column(name = "URL_VISTA_PREVIA", nullable = false)
    private String urlVistaPrevia;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void antesDeGuardar() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public String getUrlDocumento() {
        return urlDocumento;
    }

    public void setUrlDocumento(String urlDocumento) {
        this.urlDocumento = urlDocumento;
    }

    public String getUrlVistaPrevia() {
        return urlVistaPrevia;
    }

    public void setUrlVistaPrevia(String urlVistaPrevia) {
        this.urlVistaPrevia = urlVistaPrevia;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
