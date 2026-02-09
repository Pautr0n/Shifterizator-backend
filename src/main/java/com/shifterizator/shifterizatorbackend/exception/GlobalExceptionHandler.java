package com.shifterizator.shifterizatorbackend.exception;

import com.shifterizator.shifterizatorbackend.auth.exception.AuthException;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidCredentialsException;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidRefreshTokenException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyValidationException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityNotFoundException;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityValidationException;
import com.shifterizator.shifterizatorbackend.availability.exception.OverlappingAvailabilityException;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursNotFoundException;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursValidationException;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayNotFoundException;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayValidationException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftAssignmentNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.user.exception.*;
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

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleLocationNotFound(LocationNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PositionNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handlePositionNotFound(PositionNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(LanguageNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleLanguageNotFound(LanguageNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleAvailabilityNotFound(AvailabilityNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AvailabilityValidationException.class)
    public ResponseEntity<ApiErrorDto> handleAvailabilityValidation(AvailabilityValidationException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OverlappingAvailabilityException.class)
    public ResponseEntity<ApiErrorDto> handleOverlappingAvailability(OverlappingAvailabilityException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SpecialOpeningHoursNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleSpecialOpeningHoursNotFound(SpecialOpeningHoursNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SpecialOpeningHoursValidationException.class)
    public ResponseEntity<ApiErrorDto> handleSpecialOpeningHoursValidation(SpecialOpeningHoursValidationException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BlackoutDayNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleBlackoutDayNotFound(BlackoutDayNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BlackoutDayValidationException.class)
    public ResponseEntity<ApiErrorDto> handleBlackoutDayValidation(BlackoutDayValidationException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ShiftTemplateNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleShiftTemplateNotFound(ShiftTemplateNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ShiftInstanceNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleShiftInstanceNotFound(ShiftInstanceNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ShiftAssignmentNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleShiftAssignmentNotFound(ShiftAssignmentNotFoundException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ShiftValidationException.class)
    public ResponseEntity<ApiErrorDto> handleShiftValidation(ShiftValidationException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorDto> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ApiErrorDto error = new ApiErrorDto(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PositionAlreadyExistsException.class)
    public ResponseEntity<ApiErrorDto> handlePositionAlreadyExists(PositionAlreadyExistsException ex) {
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
