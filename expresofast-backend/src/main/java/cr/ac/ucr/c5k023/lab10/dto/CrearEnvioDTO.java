package cr.ac.ucr.c5k023.lab10.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearEnvioDTO(
        @NotBlank(message = "El destinatario es obligatorio")
        @Size(min = 3, max = 150, message = "El destinatario debe tener entre 3 y 150 caracteres")
        String destinatario,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(min = 5, max = 250, message = "La dirección debe tener entre 5 y 250 caracteres")
        String direccionDestino,

        @NotNull(message = "El monto de flete es obligatorio")
        @Positive(message = "El monto de flete debe ser mayor que cero")
        Double montoFlete
) {
}