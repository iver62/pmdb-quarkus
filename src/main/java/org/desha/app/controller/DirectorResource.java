package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Director;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("directors")
@ApplicationScoped
@Slf4j
public class DirectorResource extends PersonResource<Director> {

    @Inject
    public DirectorResource(@PersonType(Role.DIRECTOR) PersonService<Director> directorService) {
        super(directorService);
    }

    @Override
    protected Director createEntityInstance() {
        return Director.builder().build();
    }

}
