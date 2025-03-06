package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Director;
import org.desha.app.service.DirectorService;

@Path("directors")
@Singleton
@Slf4j
public class DirectorResource extends PersonResource<Director> {

    @Inject
    public DirectorResource(DirectorService directorService) {
        super(directorService);
    }

}
