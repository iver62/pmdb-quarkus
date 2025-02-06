package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Caster;
import org.desha.app.domain.entity.Stuntman;

@ApplicationScoped
public class StuntmanRepository extends PersonRepository<Stuntman> {
}