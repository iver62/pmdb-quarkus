package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.MakeupArtist;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("makeup-artists")
@ApplicationScoped
@Slf4j
public class MakeupArtistResource extends PersonResource<MakeupArtist> {

    @Inject
    public MakeupArtistResource(@PersonType(Role.MAKEUP_ARTIST) PersonService<MakeupArtist> makeupArtistService) {
        super(makeupArtistService, MakeupArtist.class);
    }

    @Override
    protected MakeupArtist createEntityInstance() {
        return MakeupArtist.builder().build();
    }

}
