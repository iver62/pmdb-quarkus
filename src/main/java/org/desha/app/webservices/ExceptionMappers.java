package org.desha.app.webservices;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<String> mapException(ConstraintViolationException x) {
        return RestResponse.status(Response.Status.CONFLICT, "Le genre existe déjà");
    }
}
