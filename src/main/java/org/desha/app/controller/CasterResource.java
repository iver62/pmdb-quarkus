package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Caster;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("casters")
@ApplicationScoped
@Slf4j
public class CasterResource extends PersonResource<Caster> {

    @Inject
    public CasterResource(@PersonType(Role.CASTER) PersonService<Caster> casterService) {
        super(casterService, Caster.class);
    }

    @Override
    protected Caster createEntityInstance() {
        return Caster.builder().build();
    }

}
