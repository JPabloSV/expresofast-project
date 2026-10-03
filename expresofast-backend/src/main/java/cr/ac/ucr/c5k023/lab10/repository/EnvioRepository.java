package cr.ac.ucr.c5k023.lab10.repository;

import cr.ac.ucr.c5k023.lab10.model.Envio;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    /*
     * @EntityGraph trae en la misma consulta los paquetes, el vehículo y el conductor,
     * evitando el problema N+1 al convertir cada envío a DTO.
     */
    @EntityGraph(attributePaths = {"paquetes", "vehiculo", "conductor"})
    List<Envio> findAllBy(Sort sort);

    @EntityGraph(attributePaths = {"paquetes", "vehiculo", "conductor"})
    List<Envio> findByEstado(String estado, Sort sort);

    @EntityGraph(attributePaths = {"paquetes", "vehiculo", "conductor"})
    Optional<Envio> findByCodigoRastreo(String codigoRastreo);

    /** Consulta liviana (SELECT ... EXISTS) usada por la validación asíncrona del frontend. */
    boolean existsByCodigoRastreo(String codigoRastreo);
}
