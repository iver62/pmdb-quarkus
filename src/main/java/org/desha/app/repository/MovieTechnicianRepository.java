package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.MovieTechnician;

@ApplicationScoped
public class MovieTechnicianRepository implements PanacheRepositoryBase<MovieTechnician, Long> {
}