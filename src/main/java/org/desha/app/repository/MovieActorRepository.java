package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.MovieActor;

@Slf4j
@ApplicationScoped
public class MovieActorRepository implements PanacheRepository<MovieActor> {
}