package cr.ac.ucr.c5k023.lab10.dto;

import java.time.LocalDateTime;

public record EnvioDTO(
        Long id,
        String codigoRastreo,
        String destinatario,
        String direccionDestino,
        Double montoFlete,
        String estado,
        LocalDateTime fechaCreacion
) {
}