package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Caster;
import org.desha.app.service.CasterService;

@Path("casters")
@Singleton
public class CasterResource extends PersonResource<Caster> {

    @Inject
    public CasterResource(CasterService casterService) {
        super(casterService);
    }

}
