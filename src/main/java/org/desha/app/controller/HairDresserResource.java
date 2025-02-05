package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.HairDresser;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("hair-dressers")
@ApplicationScoped
@Slf4j
public class HairDresserResource extends PersonResource<HairDresser> {

    @Inject
    public HairDresserResource(@PersonType(Role.HAIR_DRESSER) PersonService<HairDresser> hairDresserService) {
        super(hairDresserService);
    }

    @Override
    protected HairDresser createEntityInstance() {
        return HairDresser.builder().build();
    }

}
