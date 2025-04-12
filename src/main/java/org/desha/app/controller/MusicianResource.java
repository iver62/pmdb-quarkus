package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Musician;
import org.desha.app.service.MusicianService;

@Path("musicians")
@Singleton
public class MusicianResource extends PersonResource<Musician> {

    @Inject
    public MusicianResource(MusicianService musicianService) {
        super(musicianService);
    }

}
