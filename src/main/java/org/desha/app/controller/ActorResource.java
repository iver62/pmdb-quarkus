package org.desha.app.controller;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Actor;
import org.desha.app.service.ActorService;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Slf4j
@Path("actors")
@Singleton
public class ActorResource extends PersonResource<Actor> {

    private final ActorService actorService;

    @Inject
    public ActorResource(ActorService actorService) {
        super(actorService);
        this.actorService = actorService;
    }

    @GET
    @Path("/count-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<Long> streamActorCount() {
        return actorService.getActorCountStream();
    }

}
