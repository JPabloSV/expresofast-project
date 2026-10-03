package cr.ac.ucr.c5k023.lab10.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Un paquete (ítem) dentro de un envío. Se usa tanto para recibir el POST como para responder.
 * Los límites de pesoKg corresponden a la columna DECIMAL(5,2) de la tabla PAQUETES.
 */
public record PaqueteDTO(
        Long id,

        @NotBlank(message = "La descripción del paquete es obligatoria")
        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion,

        @NotNull(message = "El peso del paquete es obligatorio")
        @DecimalMin(value = "0.01", message = "El peso del paquete debe ser mayor que cero")
        @DecimalMax(value = "999.99", message = "El peso del paquete no puede superar 999.99 kg")
        @Digits(integer = 3, fraction = 2, message = "El peso admite como máximo 2 decimales")
        BigDecimal pesoKg
) {
}
