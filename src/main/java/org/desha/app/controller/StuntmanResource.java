package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Stuntman;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("stuntmen")
@ApplicationScoped
@Slf4j
public class StuntmanResource extends PersonResource<Stuntman> {

    @Inject
    public StuntmanResource(@PersonType(Role.STUNT_MAN) PersonService<Stuntman> stuntmanService) {
        super(stuntmanService);
    }

    @Override
    protected Stuntman createEntityInstance() {
        return Stuntman.builder().build();
    }

}
