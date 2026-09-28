package cr.ac.ucr.c5k023.lab10.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorDTO(
        LocalDateTime timestamp,
        int status,
        String mensaje,
        List<String> detalles
) {
}