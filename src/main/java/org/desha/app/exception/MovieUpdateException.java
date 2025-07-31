package org.desha.app.exception;

public class MovieUpdateException extends RuntimeException {

    public MovieUpdateException(String message) {
        super(message);
    }

    public MovieUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
