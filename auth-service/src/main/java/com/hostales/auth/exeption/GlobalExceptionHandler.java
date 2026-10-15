package com.hostales.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
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
    public ResponseEntity<ApiErrorResponse> negocio(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus estado = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        return construir(
                estado,
                ex.getReason(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validacion(
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

        return construir(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                request,
                errores
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> accesoDenegado(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return construir(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para realizar esta operación",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> general(
            Exception ex,
            HttpServletRequest request
    ) {
        return construir(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en auth-service",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> construir(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest request,
            Map<String, String> errores
    ) {
        ApiErrorResponse respuesta =
                ApiErrorResponse.builder()
                        .timestamp(OffsetDateTime.now())
                        .status(estado.value())
                        .error(estado.getReasonPhrase())
                        .message(mensaje)
                        .path(request.getRequestURI())
                        .validationErrors(errores)
                        .build();

        return ResponseEntity
                .status(estado)
                .body(respuesta);
    }
}
