
package pe.edu.upeu.PPPBACKEND.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "EMPRESA_PPP")
public class EmpresaPpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "CONTACTO")
    private String contacto;

    @Column(name = "REGLAMENTO")
    private String reglamento;

    @Column(name = "RECOMENDADA")
    private String recomendada = "N";

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getReglamento() {
        return reglamento;
    }

    public void setReglamento(String reglamento) {
        this.reglamento = reglamento;
    }

    public String getRecomendada() {
        return recomendada;
    }

    public void setRecomendada(String recomendada) {
        this.recomendada = recomendada;
    }
}
