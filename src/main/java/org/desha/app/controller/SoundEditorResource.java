package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.SoundEditor;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("sound-editors")
@ApplicationScoped
@Slf4j
public class SoundEditorResource extends PersonResource<SoundEditor> {

    @Inject
    public SoundEditorResource(@PersonType(Role.SOUND_EDITOR) PersonService<SoundEditor> soundEditorService) {
        super(soundEditorService, SoundEditor.class);
    }

    @Override
    protected SoundEditor createEntityInstance() {
        return SoundEditor.builder().build();
    }

}
