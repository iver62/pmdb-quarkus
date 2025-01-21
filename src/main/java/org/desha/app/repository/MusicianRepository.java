package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Musician;

@ApplicationScoped
public class MusicianRepository extends PersonRepository<Musician> {
}
