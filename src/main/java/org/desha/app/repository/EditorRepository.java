package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Editor;

@ApplicationScoped
public class EditorRepository extends PersonRepository<Editor> {
}