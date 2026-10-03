package cr.ac.ucr.c5k023.lab10.controller;

import cr.ac.ucr.c5k023.lab10.dto.ErrorDTO;
import cr.ac.ucr.c5k023.lab10.exception.EnvioNoEncontradoException;
import cr.ac.ucr.c5k023.lab10.exception.ReglaNegocioException;
import cr.ac.ucr.c5k023.lab10.exception.TrackingDuplicadoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EnvioNoEncontradoException.class)
    public ResponseEntity<ErrorDTO> envioNoEncontrado(EnvioNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(TrackingDuplicadoException.class)
    public ResponseEntity<ErrorDTO> trackingDuplicado(TrackingDuplicadoException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorDTO> reglaNegocio(ReglaNegocioException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    /** Respaldo: restricción UNIQUE/FK/CHECK violada en SQL Server (p. ej. dos registros simultáneos). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> integridad(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos; no se guardó ningún dato", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> datosInvalidos(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", List.of());
    }

    private ResponseEntity<ErrorDTO> construir(HttpStatus status, String mensaje, List<String> detalles) {
        ErrorDTO cuerpo = new ErrorDTO(LocalDateTime.now(), status.value(), mensaje, detalles);
        return ResponseEntity.status(status).body(cuerpo);
    }
}