package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Photographer;
import org.desha.app.service.PhotographerService;

@Path("photographers")
@Singleton
public class PhotographerResource extends PersonResource<Photographer> {

    @Inject
    public PhotographerResource(PhotographerService photographerService) {
        super(photographerService);
    }

}
