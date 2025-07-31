package org.desha.app.controller;

import jakarta.ws.rs.BadRequestException;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ValidationUtils {

    public void validateIdOrThrow(Long id, String errorMessage) {
        if (Objects.isNull(id) || id <= 0) {
            throw new BadRequestException(errorMessage);
        }
    }
}
