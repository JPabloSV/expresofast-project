package cr.ac.ucr.c5k023.lab10.service;

import cr.ac.ucr.c5k023.lab10.dto.CrearEnvioDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioDTO;

import java.util.List;

public interface EnvioService {

    List<EnvioDTO> obtenerTodos();

    List<EnvioDTO> obtenerPorEstado(String estado);

    EnvioDTO buscarPorCodigoRastreo(String codigo);

    EnvioDTO registrar(CrearEnvioDTO dto);

    EnvioDTO actualizarEstado(Long id, String nuevoEstado);
}