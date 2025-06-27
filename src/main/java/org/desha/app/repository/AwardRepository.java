package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Award;

@ApplicationScoped
public class AwardRepository implements PanacheRepository<Award> {
}
