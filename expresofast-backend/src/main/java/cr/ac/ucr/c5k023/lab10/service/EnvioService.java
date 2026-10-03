package cr.ac.ucr.c5k023.lab10.service;

import cr.ac.ucr.c5k023.lab10.dto.ConductorDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioRegistroDTO;
import cr.ac.ucr.c5k023.lab10.dto.VehiculoDTO;

import java.util.List;

public interface EnvioService {

    List<EnvioDTO> obtenerTodos();

    List<EnvioDTO> obtenerPorEstado(String estado);

    EnvioDTO buscarPorCodigoRastreo(String codigo);

    boolean existeTracking(String numeroTracking);

    EnvioDTO registrar(EnvioRegistroDTO dto);

    EnvioDTO actualizarEstado(Integer id, String nuevoEstado);

    List<VehiculoDTO> obtenerVehiculos();

    List<ConductorDTO> obtenerConductoresActivos();
}
