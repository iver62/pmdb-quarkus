package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Stuntman;
import org.desha.app.service.StuntmanService;

@Path("stuntmen")
@Singleton
public class StuntmanResource extends PersonResource<Stuntman> {

    @Inject
    public StuntmanResource(StuntmanService stuntmanService) {
        super(stuntmanService);
    }

}
