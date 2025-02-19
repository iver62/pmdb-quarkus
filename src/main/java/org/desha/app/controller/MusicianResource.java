package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Musician;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("musicians")
@ApplicationScoped
@Slf4j
public class MusicianResource extends PersonResource<Musician> {

    @Inject
    public MusicianResource(@PersonType(Role.MUSICIAN) PersonService<Musician> musicianService) {
        super(musicianService, Musician.class);
    }

    @Override
    protected Musician createEntityInstance() {
        return Musician.builder().build();
    }

}
