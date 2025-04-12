package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.service.CostumierService;

@Path("costumiers")
@Singleton
public class CostumierResource extends PersonResource<Costumier> {

    @Inject
    public CostumierResource(CostumierService costumierService) {
        super(costumierService);
    }

}
