package org.desha.app.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private String error;
    private String invalidField;
    private String details;

    public static ErrorResponse build(String error, String invalidField, String details) {
        return ErrorResponse.builder()
                .error(error)
                .invalidField(invalidField)
                .details(details)
                .build();
    }
}
