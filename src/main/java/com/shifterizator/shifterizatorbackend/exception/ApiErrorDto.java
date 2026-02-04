package com.shifterizator.shifterizatorbackend.exception;

public record ApiErrorDto(String message,
                          String error,
                          int status
) {
}
