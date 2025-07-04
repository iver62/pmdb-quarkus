package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.entity.Fruit;
import org.jboss.logging.Logger;

import java.util.List;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("fruits")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FruitResource {

    private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());

    @GET
    public Uni<List<Fruit>> get() {
        return Fruit.listAll(Sort.by("name"));
    }

    @GET
    @Path("{id}")
    public Uni<Fruit> getSingle(Long id) {
        return Fruit.findById(id);
    }

    @POST
    public Uni<Response> create(Fruit fruit) {
        if (fruit == null || fruit.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return Panache.withTransaction(fruit::persist)
                .replaceWith(Response.ok(fruit).status(CREATED)::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, Fruit fruit) {
        if (fruit == null || fruit.name == null) {
            throw new WebApplicationException("Fruit name was not set on request.", 422);
        }

        return Panache
                .withTransaction(() -> Fruit.<Fruit>findById(id)
                        .onItem().ifNotNull().invoke(entity -> entity.name = fruit.name)
                )
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Panache.withTransaction(() -> Fruit.deleteById(id))
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
    }

    /**
     * Create a HTTP response from an exception.
     * <p>
     * Response Example:
     *
     * <pre>
     * HTTP/1.1 422 Unprocessable Entity
     * Content-Length: 111
     * Content-Type: application/json
     *
     * {
     *     "code": 422,
     *     "error": "Fruit name was not set on request.",
     *     "exceptionType": "jakarta.ws.rs.WebApplicationException"
     * }
     * </pre>
     */
//    @Provider
//    public static class ErrorMapper implements ExceptionMapper<Exception> {
//
//        @Inject
//        ObjectMapper objectMapper;
//
//        @Override
//        public Response toResponse(Exception exception) {
//            LOGGER.error("Failed to handle request", exception);
//
//            Throwable throwable = exception;
//
//            int code = 500;
//            if (throwable instanceof WebApplicationException) {
//                code = ((WebApplicationException) exception).getResponse().getStatus();
//            }
//
//            // This is a Mutiny exception and it happens, for example, when we try to insert a new
//            // fruit but the name is already in the database
//            if (throwable instanceof CompositeException) {
//                throwable = ((CompositeException) throwable).getCause();
//            }
//
//            ObjectNode exceptionJson = objectMapper.createObjectNode();
//            exceptionJson.put("exceptionType", throwable.getClass().getName());
//            exceptionJson.put("code", code);
//
//            if (Objects.nonNull(exception.getMessage())) {
//                exceptionJson.put("error", throwable.getMessage());
//            }
//
//            return Response.status(code)
//                    .entity(exceptionJson)
//                    .build();
//        }
//
//    }
}
