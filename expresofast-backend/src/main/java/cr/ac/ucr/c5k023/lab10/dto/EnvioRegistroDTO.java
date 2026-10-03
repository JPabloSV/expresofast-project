package cr.ac.ucr.c5k023.lab10.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cuerpo del POST /api/envios enviado por EnvioAvanzadoFormComponent:
 * un envío completo junto con la lista de sus paquetes.
 */
public record EnvioRegistroDTO(
        @NotBlank(message = "El número de tracking es obligatorio")
        @Pattern(regexp = "^EXP-\\d{4}$", message = "Formato inválido. Ejemplo: EXP-1234")
        String numeroTracking,

        @NotBlank(message = "El destinatario es obligatorio")
        @Size(min = 3, max = 150, message = "El destinatario debe tener entre 3 y 150 caracteres")
        String destinatario,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
        String direccionDestino,

        @NotNull(message = "El monto de flete es obligatorio")
        @Positive(message = "El monto de flete debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El monto de flete admite como máximo 2 decimales")
        BigDecimal montoFlete,

        @NotNull(message = "La fecha de despacho es obligatoria")
        LocalDate fechaDespacho,

        @NotNull(message = "La fecha de entrega estimada es obligatoria")
        LocalDate fechaEntregaEstimada,

        @NotNull(message = "El vehículo es obligatorio")
        Integer vehiculoId,

        @NotNull(message = "El conductor es obligatorio")
        Integer conductorId,

        @NotEmpty(message = "El envío debe contener al menos un paquete")
        List<@Valid PaqueteDTO> paquetes
) {
}
