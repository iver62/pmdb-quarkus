package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("art-directors")
@ApplicationScoped
@Slf4j
public class ArtDirectorResource extends PersonResource<ArtDirector> {

    @Inject
    public ArtDirectorResource(@PersonType(Role.ART_DIRECTOR) PersonService<ArtDirector> artDirectorService) {
        super(artDirectorService);
    }

    @Override
    protected ArtDirector createEntityInstance() {
        return ArtDirector.builder().build();
    }

}
