package cr.ac.ucr.c5k023.lab10.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ActualizarEstadoDTO(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "PENDIENTE|EN_TRANSITO|ENTREGADO|CANCELADO",
                message = "Estado inválido. Valores permitidos: PENDIENTE, EN_TRANSITO, ENTREGADO, CANCELADO"
        )
        String estado
) {
}