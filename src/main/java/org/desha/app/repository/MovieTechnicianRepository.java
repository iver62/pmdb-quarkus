package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.MovieTechnician;

@Slf4j
@ApplicationScoped
public class MovieTechnicianRepository implements PanacheRepositoryBase<MovieTechnician, Long> {
}