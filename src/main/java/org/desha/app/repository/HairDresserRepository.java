package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.HairDresser;

@ApplicationScoped
public class HairDresserRepository extends PersonRepository<HairDresser> {
}
