package org.desha.app.controller;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ValidationUtilsTest {

    @Test
    void shouldThrowBadRequestIfIdIsNull() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> ValidationUtils.validateIdOrThrow(null, "Error message")
        );
        assertEquals("Error message", exception.getMessage());
    }

    @Test
    void shouldThrowBadRequestIfIdIsNegative() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> ValidationUtils.validateIdOrThrow(-5L, "Error message")
        );
        assertEquals("Error message", exception.getMessage());
    }

    @Test
    void shouldNotThrowIfIdIsValid() {
        assertDoesNotThrow(() -> ValidationUtils.validateIdOrThrow(42L, "Error message"));
    }
}
