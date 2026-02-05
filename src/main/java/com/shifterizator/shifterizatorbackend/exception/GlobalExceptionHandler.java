package com.shifterizator.shifterizatorbackend.exception;

import com.shifterizator.shifterizatorbackend.auth.exception.AuthException;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidCredentialsException;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidRefreshTokenException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyValidationException;
import com.shifterizator.shifterizatorbackend.user.exception.ForbiddenOperationException;
import com.shifterizator.shifterizatorbackend.user.exception.InvalidPasswordException;
import com.shifterizator.shifterizatorbackend.user.exception.UserAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleUserNotFound(UserNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorDto> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiErrorDto> handleForbidden(ForbiddenOperationException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "FORBIDDEN",
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorDto> handleInvalidPassword(InvalidPasswordException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorDto> handleInvalidCredentials(InvalidCredentialsException ex) {

        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "INVALID_CREDENTIALS",
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorDto> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {

        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "INVALID_REFRESH_TOKEN",
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorDto> handleAuthException(AuthException ex) {

        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "AUTH_ERROR",
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleCompanyNotFound(CompanyNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CompanyValidationException.class)
    public ResponseEntity<ApiErrorDto> handleCompanyValidation(CompanyValidationException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ApiErrorDto error = new ApiErrorDto(
                message,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneric(Exception ex) {
        ApiErrorDto error = new ApiErrorDto(
                "Unexpected error: " + ex.getMessage(),
                "INTERNAL_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


}
