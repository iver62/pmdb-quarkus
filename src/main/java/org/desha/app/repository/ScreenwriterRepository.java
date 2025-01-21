package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Screenwriter;

@ApplicationScoped
public class ScreenwriterRepository extends PersonRepository<Screenwriter> {
}
