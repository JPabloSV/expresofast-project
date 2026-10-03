package cr.ac.ucr.c5k023.lab10.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Catálogo de solo lectura sobre la tabla dbo.Conductor (labs 5 a 9). */
@Entity
@Immutable
@Table(name = "Conductor")
public class Conductor {

    @Id
    @Column(name = "conductor_id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 50)
    private String apellidos;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
}
