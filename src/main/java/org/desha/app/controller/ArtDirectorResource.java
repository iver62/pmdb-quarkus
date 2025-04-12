package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.service.ArtDirectorService;

@Path("art-directors")
@Singleton
public class ArtDirectorResource extends PersonResource<ArtDirector> {

    @Inject
    public ArtDirectorResource(ArtDirectorService artDirectorService) {
        super(artDirectorService);
    }

}
