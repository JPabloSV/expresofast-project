package cr.ac.ucr.c5k023.lab10.exception;

public class EnvioNoEncontradoException extends RuntimeException {

    public EnvioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}