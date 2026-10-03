package cr.ac.ucr.c5k023.lab10.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EnvioDTO(
        Integer id,
        String codigoRastreo,
        String destinatario,
        String direccionDestino,
        BigDecimal montoFlete,
        BigDecimal pesoKg,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDate fechaDespacho,
        LocalDate fechaEntregaEstimada,
        Integer vehiculoId,
        String vehiculoPlaca,
        Integer conductorId,
        String conductorNombre,
        List<PaqueteDTO> paquetes
) {
}
