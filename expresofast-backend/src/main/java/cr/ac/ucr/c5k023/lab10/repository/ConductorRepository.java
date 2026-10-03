package cr.ac.ucr.c5k023.lab10.repository;

import cr.ac.ucr.c5k023.lab10.model.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConductorRepository extends JpaRepository<Conductor, Integer> {

    List<Conductor> findByActivoTrueOrderByNombreAsc();
}
