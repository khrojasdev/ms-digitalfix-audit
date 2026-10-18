package cl.duoc.digitalfix.audit.web;

import cl.duoc.digitalfix.audit.error.AccesoDenegado;
import cl.duoc.digitalfix.audit.web.dto.RespuestaError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler(AccesoDenegado.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(AccesoDenegado ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(RespuestaError.crear("ACCESO_DENEGADO", ex.getMessage()));
    }
}