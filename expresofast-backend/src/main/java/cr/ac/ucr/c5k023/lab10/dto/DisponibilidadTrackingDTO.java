package cr.ac.ucr.c5k023.lab10.dto;

/** Respuesta de GET /api/envios/check-tracking/{trackingNumber}. */
public record DisponibilidadTrackingDTO(String numeroTracking, boolean existe) {
}
