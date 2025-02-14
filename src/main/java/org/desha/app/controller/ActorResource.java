package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Actor;
import org.desha.app.service.ActorService;

@Path("actors")
@ApplicationScoped
@Slf4j
public class ActorResource extends PersonResource<Actor> {

    private final ActorService actorService;

    @Inject
    public ActorResource(ActorService actorService) {
        super(actorService);
        this.actorService = actorService;
    }

    @GET
    @Path("{id}/movie-actors")
    public Uni<Response> getMovieActors(Long id) {
        return
                actorService.getOne(id)
                        .chain(actorService::getMovieActors)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @Override
    protected Actor createEntityInstance() {
        return Actor.builder().build();
    }

}
