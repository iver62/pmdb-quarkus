package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Screenwriter;
import org.desha.app.service.ScreenwriterService;

@Path("screenwriters")
@Singleton
@Slf4j
public class ScreenwriterResource extends PersonResource<Screenwriter> {

    @Inject
    public ScreenwriterResource(ScreenwriterService screenwriterService) {
        super(screenwriterService);
    }

}
