package ilp_submission_2.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles exceptions thrown during request processing across the application.
 * <p>
 * Used to centralize error handling and keeps controller logic clean.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors triggered when request data fails to meet method-level constraints.
     * <p>
     * Logs each field that failed validation along with its error message,
     * and returns a {@code 400 Bad Request} response without a body.
     *
     * @param ex the exception containing validation failure details
     * @return a {@code ResponseEntity} with HTTP status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationException(MethodArgumentNotValidException ex) {
        ex.getBindingResult().getFieldErrors().forEach(error ->
                logger.error("Validation failed for field '{}': {}", error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().build();
    }
}
