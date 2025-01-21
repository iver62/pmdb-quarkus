package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Director;

@ApplicationScoped
public class DirectorRepository extends PersonRepository<Director> {
}