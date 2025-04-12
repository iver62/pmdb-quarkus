package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.SoundEditor;
import org.desha.app.service.SoundEditorService;

@Path("sound-editors")
@Singleton
public class SoundEditorResource extends PersonResource<SoundEditor> {

    @Inject
    public SoundEditorResource(SoundEditorService soundEditorService) {
        super(soundEditorService);
    }

}
