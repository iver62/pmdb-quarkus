package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.exception.ErrorResponse;
import org.desha.app.exception.InvalidDateException;
import org.desha.app.exception.InvalidSortException;
import org.desha.app.exception.MovieUpdateException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
@Slf4j
public class ExceptionMappers {

    @ServerExceptionMapper
    public Uni<Response> mapException(InvalidDateException exception) {
        return
                Uni.createFrom().item(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(ErrorResponse.build(exception.getMessage(), exception.getInvalidField(), exception.getDetails()))
                                .build()
                );
    }

    @ServerExceptionMapper
    public Uni<Response> mapException(InvalidSortException exception) {
        return
                Uni.createFrom().item(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(exception.getMessage())
                                .build()
                );
    }

    @ServerExceptionMapper
    public Response mapException(WebApplicationException exception) {
        return Response.status(exception.getResponse().getStatus())
                .entity(exception.getMessage())
                .build();
    }

    @ServerExceptionMapper
    public Response mapException(DateTimeParseException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Format de date invalide. Utilisez le format YYYY-MM-DD.")
                .build();
    }

    @ServerExceptionMapper
    public Response mapException(MovieUpdateException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage())
                .build();
    }

    @ServerExceptionMapper
    public Response mapException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Erreur de validation : {}", message);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", "Erreur de validation", "details", message))
                .build();
    }

}
