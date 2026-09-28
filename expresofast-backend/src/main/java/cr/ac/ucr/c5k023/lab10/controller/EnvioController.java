package cr.ac.ucr.c5k023.lab10.controller;

import cr.ac.ucr.c5k023.lab10.dto.ActualizarEstadoDTO;
import cr.ac.ucr.c5k023.lab10.dto.CrearEnvioDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioDTO;
import cr.ac.ucr.c5k023.lab10.service.EnvioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/envios")
@CrossOrigin(origins = "http://localhost:4200")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public List<EnvioDTO> listar(@RequestParam(required = false) String estado) {
        if (estado != null && !estado.isBlank()) {
            return envioService.obtenerPorEstado(estado.trim().toUpperCase(Locale.ROOT));
        }
        return envioService.obtenerTodos();
    }

    @GetMapping("/rastreo/{codigo}")
    public EnvioDTO buscarPorRastreo(@PathVariable String codigo) {
        return envioService.buscarPorCodigoRastreo(codigo);
    }

    @PostMapping
    public ResponseEntity<EnvioDTO> crear(@Valid @RequestBody CrearEnvioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.registrar(dto));
    }

    @PatchMapping("/{id}/estado")
    public EnvioDTO actualizarEstado(@PathVariable Long id,
                                     @Valid @RequestBody ActualizarEstadoDTO dto) {
        return envioService.actualizarEstado(id, dto.estado());
    }
}