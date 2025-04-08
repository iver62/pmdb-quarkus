package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.exception.ErrorResponse;
import org.desha.app.exception.InvalidDateException;
import org.desha.app.exception.InvalidSortException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.format.DateTimeParseException;

@Provider
@Slf4j
public class ExceptionMappers {

    @ServerExceptionMapper
    public Uni<Response> mapException(InvalidDateException exception) {
        log.info("InvalidDateException: {}", exception.getMessage());

        return
                Uni.createFrom().item(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(ErrorResponse.build(exception.getMessage(), exception.getInvalidField(), exception.getDetails()))
                                .build()
                );
    }

    @ServerExceptionMapper
    public Uni<Response> mapException(InvalidSortException exception) {
        log.info("InvalidSortException: {}", exception.getMessage());

        return
                Uni.createFrom().item(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(exception.getMessage())
                                .build()
                );
    }

    @ServerExceptionMapper
    public Response mapException(WebApplicationException exception) {
        log.info("WebApplicationException: {}", exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(exception.getMessage())
                .build();
    }

    @ServerExceptionMapper
    public Response mapException(DateTimeParseException exception) {
        log.info("DateTimeParseException: {}", exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Format de date invalide. Utilisez le format YYYY-MM-DD.")
                .build();
    }

}
