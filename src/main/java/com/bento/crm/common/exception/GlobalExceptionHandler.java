package com.bento.crm.common.exception;

import com.bento.crm.common.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        List<ApiError.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.add(ApiError.FieldError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .build());
        });

        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/validation")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Input validation failed")
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .validationErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleAuthenticationFailedException(
            AuthenticationFailedException ex,
            WebRequest request) {
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/unauthorized")
                .title("Authentication Failed")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/bad-request")
                .title("Bad Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/conflict")
                .title("Conflict")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/forbidden")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have permission to access this resource")
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            WebRequest request) {
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/not-found")
                .title("Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail("The requested resource was not found")
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            WebRequest request) {
        log.error("Unhandled exception", ex);
        ApiError apiError = ApiError.builder()
                .type("https://api.example.com/errors/internal-server-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred: " + ex.getMessage())
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
