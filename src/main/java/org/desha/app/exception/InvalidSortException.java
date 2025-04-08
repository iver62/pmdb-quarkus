package org.desha.app.exception;

public class InvalidSortException extends RuntimeException {

    // Constructeur sans message d'erreur
    public InvalidSortException() {
        super("Invalid sort provided.");
    }

    // Constructeur avec un message d'erreur
    public InvalidSortException(String message) {
        super(message);
    }

    // Constructeur avec message et cause
    public InvalidSortException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructeur avec cause
    public InvalidSortException(Throwable cause) {
        super(cause);
    }
}
