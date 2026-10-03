package cr.ac.ucr.c5k023.lab10.repository;

import cr.ac.ucr.c5k023.lab10.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
}
