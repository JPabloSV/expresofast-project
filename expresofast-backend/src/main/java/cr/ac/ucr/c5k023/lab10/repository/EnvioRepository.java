package cr.ac.ucr.c5k023.lab10.repository;

import cr.ac.ucr.c5k023.lab10.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    Optional<Envio> findByCodigoRastreo(String codigoRastreo);

    boolean existsByCodigoRastreo(String codigoRastreo);

    @Query("SELECT e FROM Envio e WHERE e.estado = :estado ORDER BY e.fechaCreacion DESC")
    List<Envio> buscarPorEstado(@Param("estado") String estado);
}