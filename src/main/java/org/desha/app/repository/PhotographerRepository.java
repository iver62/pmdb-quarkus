package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Photographer;

@ApplicationScoped
public class PhotographerRepository extends PersonRepository<Photographer> {
}
