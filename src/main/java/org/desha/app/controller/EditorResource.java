package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Editor;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("editors")
@ApplicationScoped
@Slf4j
public class EditorResource extends PersonResource<Editor> {

    @Inject
    public EditorResource(@PersonType(Role.EDITOR) PersonService<Editor> editorService) {
        super(editorService);
    }

    @Override
    protected Editor createEntityInstance() {
        return Editor.builder().build();
    }

}
