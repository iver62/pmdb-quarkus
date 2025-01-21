package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.ArtDirector;

@ApplicationScoped
public class ArtDirectorRepository extends PersonRepository<ArtDirector> {
}
