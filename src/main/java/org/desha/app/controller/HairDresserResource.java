package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.HairDresser;
import org.desha.app.service.HairDresserService;

@Path("hair-dressers")
@Singleton
@Slf4j
public class HairDresserResource extends PersonResource<HairDresser> {

    @Inject
    public HairDresserResource(HairDresserService hairDresserService) {
        super(hairDresserService);
    }

}
