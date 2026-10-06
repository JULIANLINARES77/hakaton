package uniminuto.campusreporta.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import uniminuto.campusreporta.dto.ApiErrorDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorDTO> handleBusiness(BusinessException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<ApiErrorDTO> handleAiProvider(AiProviderException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatus());
        return response(status, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleBodyValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos.", request, errors);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiErrorDTO> handleParameterValidation(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Uno o más parámetros no cumplen las validaciones.", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorDTO> handleParameterTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST,
                "El parámetro '" + exception.getName() + "' tiene un formato o valor no válido.",
                request, Map.of(exception.getName(), "Revisa el formato y los valores permitidos."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST,
                "El JSON no es válido o contiene un valor de estado/rol no reconocido.", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        LOGGER.warn("La solicitud violó una restricción de integridad en {}: {}", request.getRequestURI(), exception.getMostSpecificCause().getMessage());
        return response(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes o restricciones de la base de datos.",
                request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Error no controlado atendiendo {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud.", request, Map.of());
    }

    private ResponseEntity<ApiErrorDTO> response(
            HttpStatus status, String message, HttpServletRequest request, Map<String, String> validationErrors) {
        ApiErrorDTO body = new ApiErrorDTO(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
