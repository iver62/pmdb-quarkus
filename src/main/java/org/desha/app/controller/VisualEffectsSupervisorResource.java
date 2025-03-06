package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.VisualEffectsSupervisor;
import org.desha.app.service.VisualEffectsSupervisorService;

@Path("visual-effects-supervisors")
@Singleton
@Slf4j
public class VisualEffectsSupervisorResource extends PersonResource<VisualEffectsSupervisor> {

    @Inject
    public VisualEffectsSupervisorResource(VisualEffectsSupervisorService visualEffectsSupervisorService) {
        super(visualEffectsSupervisorService);
    }

}
