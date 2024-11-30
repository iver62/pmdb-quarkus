package org.desha.app.webservices;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Award;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

import java.util.List;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@Path("awards")
@ApplicationScoped
@Slf4j
public class AwardResource {

    @Inject
    SessionFactory sf;

    @GET
    public Uni<List<Award>> get() {
        return Award.listAll();
    }

    @GET
    @Path("{id}")
    public Uni<Award> getSingle(Long id) {
        return Award.findById(id);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return
                Panache
                        .withTransaction(() -> Award.deleteById(id))
                        .map(deleted -> deleted
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build());
    }

}
