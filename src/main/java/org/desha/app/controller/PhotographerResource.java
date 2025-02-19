package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Photographer;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("photographers")
@ApplicationScoped
@Slf4j
public class PhotographerResource extends PersonResource<Photographer> {

    @Inject
    public PhotographerResource(@PersonType(Role.PHOTOGRAPHER) PersonService<Photographer> photographerService) {
        super(photographerService, Photographer.class);
    }

    @Override
    protected Photographer createEntityInstance() {
        return Photographer.builder().build();
    }

}
