package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Editor;
import org.desha.app.service.EditorService;

@Path("editors")
@Singleton
@Slf4j
public class EditorResource extends PersonResource<Editor> {

    @Inject
    public EditorResource(EditorService editorService) {
        super(editorService);
    }

}
