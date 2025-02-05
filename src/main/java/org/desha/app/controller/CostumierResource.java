package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("costumiers")
@ApplicationScoped
@Slf4j
public class CostumierResource extends PersonResource<Costumier> {

    @Inject
    public CostumierResource(@PersonType(Role.COSTUMIER) PersonService<Costumier> costumierService) {
        super(costumierService);
    }

    @Override
    protected Costumier createEntityInstance() {
        return Costumier.builder().build();
    }

}
