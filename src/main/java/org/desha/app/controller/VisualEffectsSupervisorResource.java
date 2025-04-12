package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.VisualEffectsSupervisor;
import org.desha.app.service.VisualEffectsSupervisorService;

@Path("visual-effects-supervisors")
@Singleton
public class VisualEffectsSupervisorResource extends PersonResource<VisualEffectsSupervisor> {

    @Inject
    public VisualEffectsSupervisorResource(VisualEffectsSupervisorService visualEffectsSupervisorService) {
        super(visualEffectsSupervisorService);
    }

}
