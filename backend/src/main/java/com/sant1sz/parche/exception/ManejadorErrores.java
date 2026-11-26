package com.sant1sz.parche.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Manejo centralizado de errores. Toda respuesta de error usa el mismo cuerpo (RFC 9457;
 * "type" se omite porque siempre es "about:blank"):
 *
 * <pre>
 * {
 *   "title": "Recurso no encontrado",
 *   "status": 404,
 *   "detail": "No existe un negocio activo con id 999.",
 *   "instance": "/api/negocios/999/menu",
 *   "codigo": "RECURSO_NO_ENCONTRADO",
 *   "marcaTiempo": "2026-11-26T22:30:00Z",
 *   "errores": [ { "campo": "id", "mensaje": "..." } ]   // solo en errores de validación
 * }
 * </pre>
 *
 * Los errores propios de Spring (ruta inexistente, método no permitido, etc.) pasan por
 * {@link #handleExceptionInternal} y salen con el mismo formato y textos en español.
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    private static final String CODIGO = "codigo";
    private static final String MARCA_TIEMPO = "marcaTiempo";
    private static final String ERRORES = "errores";

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<Object> recursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        ProblemDetail cuerpo = problema(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage());
        return handleExceptionInternal(ex, cuerpo, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * Último recurso. Si la excepción declara su estado con {@link ResponseStatus}, se respeta
     * (no es un error del servidor); si no, responde 500 y registra el error.
     *
     * <p>Pendiente para la fase 1: cuando entre Spring Security, sus excepciones
     * ({@code AccessDeniedException}, {@code AuthenticationException}) no deben caer aquí.
     * Hay que manejarlas en el {@code AccessDeniedHandler} y el {@code AuthenticationEntryPoint}
     * de la cadena de seguridad, o en manejadores propios que respondan 403 y 401 con este mismo cuerpo.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> inesperado(Exception ex, WebRequest request) {
        ResponseStatus anotacion = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (anotacion != null) {
            HttpStatus status = anotacion.code();
            TextoError texto = TextoError.para(status);
            String detalle = StringUtils.hasText(anotacion.reason()) ? anotacion.reason() : texto.detalle();
            log.debug("Excepción con @ResponseStatus({}) en {}: {}", status.value(), ruta(request), ex.toString());
            ProblemDetail cuerpo = problema(status, texto.codigo(), detalle);
            return handleExceptionInternal(ex, cuerpo, new HttpHeaders(), status, request);
        }

        log.error("Error no controlado en {}", ruta(request), ex);
        ProblemDetail cuerpo = problema(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intenta de nuevo en un momento.");
        return handleExceptionInternal(ex, cuerpo, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Restricciones en los parámetros del método: de ruta o de consulta (@Positive, @Pattern...),
     * y también el cuerpo @Valid cuando el método combina ambos. En ese caso los errores del cuerpo
     * llegan como {@link FieldError} y se reportan con el nombre del campo, no con el del parámetro.
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorCampo> errores = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(error -> error instanceof FieldError fieldError
                                ? new ErrorCampo(fieldError.getField(), mensajeCampo(fieldError))
                                : new ErrorCampo(
                                        resultado.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return validacion(ex, errores, headers, request);
    }

    /** Cuerpos de petición con @Valid que no pasan la validación. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorCampo(error.getField(), mensajeCampo(error)))
                .toList();
        return validacion(ex, errores, headers, request);
    }

    /** Por ejemplo, /api/negocios/abc/menu: el id no es un número. */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String campo = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : ex.getPropertyName();
        String mensaje = esNumero(ex.getRequiredType())
                ? "Debe ser un número entero."
                : "No tiene un formato válido.";
        return validacion(ex, List.of(new ErrorCampo(campo, mensaje)), headers, request);
    }

    /**
     * Punto por el que pasan todas las respuestas de error. Completa el cuerpo uniforme y
     * reemplaza los textos en inglés de Spring cuando el error no lo generamos nosotros.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ProblemDetail cuerpo = body instanceof ProblemDetail pd ? pd : ProblemDetail.forStatus(statusCode);

        if (cuerpo.getProperties() == null || !cuerpo.getProperties().containsKey(CODIGO)) {
            TextoError texto = TextoError.para(statusCode);
            cuerpo.setTitle(texto.titulo());
            cuerpo.setDetail(texto.detalle());
            cuerpo.setProperty(CODIGO, texto.codigo());
        }
        if (cuerpo.getInstance() == null) {
            cuerpo.setInstance(URI.create(ruta(request)));
        }
        cuerpo.setProperty(MARCA_TIEMPO, Instant.now());

        return super.handleExceptionInternal(ex, cuerpo, headers, statusCode, request);
    }

    private ResponseEntity<Object> validacion(
            Exception ex, List<ErrorCampo> errores, HttpHeaders headers, WebRequest request) {
        ProblemDetail cuerpo = problema(HttpStatus.BAD_REQUEST, "VALIDACION",
                "Algunos datos de la solicitud no son válidos.");
        cuerpo.setProperty(ERRORES, errores);
        return handleExceptionInternal(ex, cuerpo, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static ProblemDetail problema(HttpStatus status, String codigo, String detalle) {
        ProblemDetail cuerpo = ProblemDetail.forStatusAndDetail(status, detalle);
        cuerpo.setTitle(TextoError.para(status).titulo());
        cuerpo.setProperty(CODIGO, codigo);
        return cuerpo;
    }

    private static String mensajeCampo(FieldError error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "No es válido.";
    }

    private static boolean esNumero(Class<?> tipo) {
        return tipo != null && (Number.class.isAssignableFrom(tipo)
                || tipo == long.class || tipo == int.class || tipo == short.class);
    }

    private static String ruta(WebRequest request) {
        return request instanceof ServletWebRequest servlet
                ? servlet.getRequest().getRequestURI()
                : request.getDescription(false).replaceFirst("^uri=", "");
    }

    /** Textos en español para los estados HTTP más comunes. */
    private record TextoError(String titulo, String detalle, String codigo) {

        static TextoError para(HttpStatusCode status) {
            return switch (status.value()) {
                case 400 -> new TextoError("Solicitud inválida",
                        "La solicitud no es válida.", "SOLICITUD_INVALIDA");
                case 401 -> new TextoError("No autenticado",
                        "Debes iniciar sesión para continuar.", "NO_AUTENTICADO");
                case 403 -> new TextoError("Acceso denegado",
                        "No tienes permiso para hacer esta operación.", "ACCESO_DENEGADO");
                case 409 -> new TextoError("Conflicto",
                        "La operación entra en conflicto con el estado actual de los datos.", "CONFLICTO");
                case 404 -> new TextoError("Recurso no encontrado",
                        "No encontramos lo que buscas.", "RECURSO_NO_ENCONTRADO");
                case 405 -> new TextoError("Método no permitido",
                        "Esta operación no está permitida en esta ruta.", "METODO_NO_PERMITIDO");
                case 406 -> new TextoError("Formato no aceptable",
                        "No podemos responder en el formato solicitado.", "FORMATO_NO_ACEPTABLE");
                case 415 -> new TextoError("Tipo de contenido no soportado",
                        "El tipo de contenido de la solicitud no es soportado.", "TIPO_NO_SOPORTADO");
                case 500 -> new TextoError("Error interno",
                        "Ocurrió un error inesperado. Intenta de nuevo en un momento.", "ERROR_INTERNO");
                default -> status.is4xxClientError()
                        ? new TextoError("Error en la solicitud",
                                "No pudimos procesar la solicitud.", "ERROR_SOLICITUD")
                        : new TextoError("Error del servidor",
                                "El servidor no pudo completar la solicitud.", "ERROR_SERVIDOR");
            };
        }
    }
}
