package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Actor;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("actors")
@ApplicationScoped
@Slf4j
public class ActorResource extends PersonResource<Actor> {

    @Inject
    public ActorResource(@PersonType(Role.ACTOR) PersonService<Actor> actorService) {
        super(actorService, Actor.class);
    }

    @Override
    protected Actor createEntityInstance() {
        return Actor.builder().build();
    }
}
