package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Musician;
import org.desha.app.service.MusicianService;

@Path("musicians")
@Singleton
@Slf4j
public class MusicianResource extends PersonResource<Musician> {

    @Inject
    public MusicianResource(MusicianService musicianService) {
        super(musicianService);
    }

}
