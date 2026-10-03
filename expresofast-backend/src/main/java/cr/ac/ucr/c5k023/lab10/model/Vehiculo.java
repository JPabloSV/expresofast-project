package cr.ac.ucr.c5k023.lab10.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/** Catálogo de solo lectura sobre la tabla dbo.Vehiculo (labs 5 a 9). */
@Entity
@Immutable
@Table(name = "Vehiculo")
public class Vehiculo {

    @Id
    @Column(name = "vehiculo_id")
    private Integer id;

    @Column(name = "placa", nullable = false, length = 15)
    private String placa;

    @Column(name = "capacidad_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadKg;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    public Integer getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public BigDecimal getCapacidadKg() {
        return capacidadKg;
    }

    public String getEstado() {
        return estado;
    }
}
