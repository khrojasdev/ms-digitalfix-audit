package cl.duoc.digitalfix.audit.error;
public class AccesoDenegado extends RuntimeException {
    public AccesoDenegado(String mensaje) { super(mensaje); }
}