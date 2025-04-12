package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.service.AwardService;

import java.util.List;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@Path("awards")
@ApplicationScoped
public class AwardResource {

    private final AwardService awardService;

    @Inject
    public AwardResource(AwardService awardService) {
        this.awardService = awardService;
    }

    @GET
    public Uni<List<Award>> get() {
        return PanacheEntityBase.listAll();
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Award> getSingle(Long id) {
        return PanacheEntityBase.findById(id);
    }

    @GET
    @Path("ceremonies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCeremonies(@BeanParam QueryParamsDTO queryParamsDTO) {
        return
                awardService.findCeremonies()
                        .onItem().ifNotNull().transform(stringList -> Response.ok(stringList).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> delete(Long id) {
        return
                Panache
                        .withTransaction(() -> PanacheEntityBase.deleteById(id))
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build());
    }

}
