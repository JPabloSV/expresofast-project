package cr.ac.ucr.c5k023.lab10.exception;

/** El número de tracking ya existe en la base de datos (HTTP 409). */
public class TrackingDuplicadoException extends RuntimeException {

    public TrackingDuplicadoException(String numeroTracking) {
        super("Este número de rastreo ya está en uso: " + numeroTracking);
    }
}
