package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Actor;
import org.desha.app.service.ActorService;

@Path("actors")
@Singleton
@Slf4j
public class ActorResource extends PersonResource<Actor> {

    @Inject
    public ActorResource(ActorService actorService) {
        super(actorService);
    }

}
