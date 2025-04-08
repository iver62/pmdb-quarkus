package org.desha.app.exception;

import lombok.Getter;

@Getter
public class InvalidDateException extends RuntimeException {

    private final String invalidField;
    private final String details;

    public InvalidDateException(String invalidField, String details) {
        super("La date de début ne peut pas être après la date de fin.");
        this.invalidField = invalidField;
        this.details = details;
    }

}
