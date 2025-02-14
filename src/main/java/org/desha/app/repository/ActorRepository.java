package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.desha.app.domain.entity.Actor;

@Named("actorRepository")
@ApplicationScoped
public class ActorRepository extends PersonRepository<Actor> {
}