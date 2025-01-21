package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Caster;

@ApplicationScoped
public class CasterRepository extends PersonRepository<Caster> {
}