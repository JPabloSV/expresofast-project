package cr.ac.ucr.c5k023.lab10.controller;

import cr.ac.ucr.c5k023.lab10.dto.ConductorDTO;
import cr.ac.ucr.c5k023.lab10.dto.VehiculoDTO;
import cr.ac.ucr.c5k023.lab10.service.EnvioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogos de solo lectura para llenar los selectores del formulario de envíos. */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final EnvioService envioService;

    public CatalogoController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/vehiculos")
    public List<VehiculoDTO> vehiculos() {
        return envioService.obtenerVehiculos();
    }

    @GetMapping("/conductores")
    public List<ConductorDTO> conductores() {
        return envioService.obtenerConductoresActivos();
    }
}
