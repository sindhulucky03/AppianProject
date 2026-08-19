package com.schwab.auditlog.api;

import com.schwab.auditlog.api.dto.ApiErrorResponse;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    ResponseEntity<ApiErrorResponse> badRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                clock.instant(), HttpStatus.BAD_REQUEST.value(), "Bad Request", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> internalError(Exception exception) {
        LOGGER.error("Unhandled API failure: type={}", exception.getClass().getSimpleName(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                clock.instant(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "Unexpected server error"));
    }
}
