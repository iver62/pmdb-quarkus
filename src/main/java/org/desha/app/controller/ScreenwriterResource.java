package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Screenwriter;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("screenwriters")
@ApplicationScoped
@Slf4j
public class ScreenwriterResource extends PersonResource<Screenwriter> {

    @Inject
    public ScreenwriterResource(@PersonType(Role.SCREENWRITER) PersonService<Screenwriter> screenwriterService) {
        super(screenwriterService, Screenwriter.class);
    }

    @Override
    protected Screenwriter createEntityInstance() {
        return Screenwriter.builder().build();
    }

}
