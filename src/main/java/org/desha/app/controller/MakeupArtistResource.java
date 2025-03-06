package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.MakeupArtist;
import org.desha.app.service.MakeupArtistService;

@Path("makeup-artists")
@Singleton
@Slf4j
public class MakeupArtistResource extends PersonResource<MakeupArtist> {

    @Inject
    public MakeupArtistResource(MakeupArtistService makeupArtistService) {
        super(makeupArtistService);
    }

}
