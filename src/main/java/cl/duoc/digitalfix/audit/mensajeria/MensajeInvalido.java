package cl.duoc.digitalfix.audit.mensajeria;

/**
 * El mensaje no se puede procesar por como viene (le falta un dato obligatorio,
 * trae un valor imposible). No se reintenta: va directo a la DLQ.
 */
public class MensajeInvalido extends RuntimeException {
    public MensajeInvalido(String mensaje) { super(mensaje); }
}
