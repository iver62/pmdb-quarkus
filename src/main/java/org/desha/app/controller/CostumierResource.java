package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.service.CostumierService;

@Path("costumiers")
@Singleton
@Slf4j
public class CostumierResource extends PersonResource<Costumier> {

    @Inject
    public CostumierResource(CostumierService costumierService) {
        super(costumierService);
    }

}
