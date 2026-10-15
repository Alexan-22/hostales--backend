package com.hostales.pago.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> manejarNegocio(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus estado = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        return ResponseEntity.status(estado).body(
                ApiErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(estado.value())
                        .error(estado.getReasonPhrase())
                        .message(ex.getReason())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError error
                : ex.getBindingResult().getFieldErrors()) {

            errores.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(400)
                        .error("Bad Request")
                        .message("Los datos enviados no son válidos")
                        .path(request.getRequestURI())
                        .validationErrors(errores)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
                ApiErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(500)
                        .error("Internal Server Error")
                        .message("Ocurrió un error interno en pago-service")
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
