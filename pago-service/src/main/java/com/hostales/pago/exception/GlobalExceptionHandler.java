package com.hostales.pago.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> manejarNegocio(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        if (ex.getStatusCode().is5xxServerError()) {
            log.error(
                    "Fallo en {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex
            );
        }

        return crearRespuesta(
                ex.getStatusCode(),
                ex.getReason() == null
                        ? "No se pudo completar la solicitud"
                        : ex.getReason(),
                request.getRequestURI(),
                null,
                ex.getHeaders()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage() == null
                            ? "Valor inválido"
                            : error.getDefaultMessage()
            );
        }

        return crearRespuesta(
                status,
                "Los datos enviados no son válidos",
                obtenerRuta(request),
                errores,
                headers
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> manejarRestricciones(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violacion ->
                errores.putIfAbsent(
                        violacion.getPropertyPath().toString(),
                        violacion.getMessage()
                )
        );

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                request.getRequestURI(),
                errores,
                HttpHeaders.EMPTY
        );
    }

    @ExceptionHandler({
            DataAccessResourceFailureException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<Object> manejarBaseNoDisponible(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Persistencia no disponible en {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        return crearRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de pagos no está disponible temporalmente",
                request.getRequestURI(),
                null,
                HttpHeaders.EMPTY
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        if (status.is5xxServerError()) {
            log.error(
                    "Error de Spring MVC en {}",
                    obtenerRuta(request),
                    ex
            );
        }

        String mensaje = switch (status.value()) {
            case 400 -> ex instanceof HttpMessageNotReadableException
                    ? "El cuerpo JSON está ausente, mal formado o contiene valores inválidos"
                    : "La solicitud contiene parámetros inválidos";
            case 404 -> "Recurso no encontrado";
            case 405 -> "El método HTTP no está permitido para esta ruta";
            case 406 -> "El formato de respuesta solicitado no está disponible";
            case 415 -> "El tipo de contenido enviado no está admitido";
            default -> status.is5xxServerError()
                    ? "Ocurrió un error interno en pago-service"
                    : "No se pudo procesar la solicitud";
        };

        return crearRespuesta(
                status,
                mensaje,
                obtenerRuta(request),
                null,
                headers
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> manejarGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Error no controlado en {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en pago-service",
                request.getRequestURI(),
                null,
                HttpHeaders.EMPTY
        );
    }

    private ResponseEntity<Object> crearRespuesta(
            HttpStatusCode status,
            String mensaje,
            String ruta,
            Map<String, String> errores,
            HttpHeaders headers
    ) {
        HttpStatus estado = HttpStatus.resolve(status.value());

        ApiErrorResponse respuesta = ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(estado == null ? "HTTP Error" : estado.getReasonPhrase())
                .message(mensaje)
                .path(ruta)
                .validationErrors(errores)
                .build();

        return new ResponseEntity<>(respuesta, headers, status);
    }

    private String obtenerRuta(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return "";
    }
}
