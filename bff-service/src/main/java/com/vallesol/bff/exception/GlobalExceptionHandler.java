package com.vallesol.bff.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Manejador global de excepciones del BFF.
 *
 * Cuando un microservicio (llamado vía RestTemplate) responde con un error
 * 4xx/5xx, RestTemplate lanza una HttpStatusCodeException. Sin este manejador,
 * el BFF la dejaba propagar y Spring devolvía un 500 genérico, ocultando el
 * status y mensaje reales (por ejemplo, el 400 "La brigada debe estar dentro
 * de la zona asignada"). Aquí reemitimos el mismo status y cuerpo al frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleDownstreamError(HttpStatusCodeException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ex.getResponseBodyAsString());
    }
}
