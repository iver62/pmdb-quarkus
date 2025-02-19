package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.VisualEffectsSupervisor;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("visual-effects-supervisors")
@ApplicationScoped
@Slf4j
public class VisualEffectsSupervisorResource extends PersonResource<VisualEffectsSupervisor> {

    @Inject
    public VisualEffectsSupervisorResource(@PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService) {
        super(visualEffectsSupervisorService, VisualEffectsSupervisor.class);
    }

    @Override
    protected VisualEffectsSupervisor createEntityInstance() {
        return VisualEffectsSupervisor.builder().build();
    }

}
