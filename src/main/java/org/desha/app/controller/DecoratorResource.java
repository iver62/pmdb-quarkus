package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Decorator;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("decorators")
@ApplicationScoped
@Slf4j
public class DecoratorResource extends PersonResource<Decorator> {

    @Inject
    public DecoratorResource(@PersonType(Role.DECORATOR) PersonService<Decorator> decoratorService) {
        super(decoratorService, Decorator.class);
    }

    @Override
    protected Decorator createEntityInstance() {
        return Decorator.builder().build();
    }

}
