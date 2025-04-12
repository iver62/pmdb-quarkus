package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Editor;
import org.desha.app.service.EditorService;

@Path("editors")
@Singleton
public class EditorResource extends PersonResource<Editor> {

    @Inject
    public EditorResource(EditorService editorService) {
        super(editorService);
    }

}
