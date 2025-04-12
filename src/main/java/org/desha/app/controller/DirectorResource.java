package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Director;
import org.desha.app.service.DirectorService;

@Path("directors")
@Singleton
public class DirectorResource extends PersonResource<Director> {

    @Inject
    public DirectorResource(DirectorService directorService) {
        super(directorService);
    }

}
