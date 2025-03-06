package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Stuntman;
import org.desha.app.service.StuntmanService;

@Path("stuntmen")
@Singleton
@Slf4j
public class StuntmanResource extends PersonResource<Stuntman> {

    @Inject
    public StuntmanResource(StuntmanService stuntmanService) {
        super(stuntmanService);
    }

}
