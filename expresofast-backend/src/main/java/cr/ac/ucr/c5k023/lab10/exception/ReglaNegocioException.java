package cr.ac.ucr.c5k023.lab10.exception;

/** Datos sintácticamente válidos que incumplen una regla de negocio (HTTP 400). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
