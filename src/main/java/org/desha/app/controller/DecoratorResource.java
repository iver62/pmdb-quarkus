package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Decorator;
import org.desha.app.service.DecoratorService;

@Path("decorators")
@Singleton
public class DecoratorResource extends PersonResource<Decorator> {

    @Inject
    public DecoratorResource(DecoratorService decoratorService) {
        super(decoratorService);
    }

}
