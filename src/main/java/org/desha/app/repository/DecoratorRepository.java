package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Decorator;

@ApplicationScoped
public class DecoratorRepository extends PersonRepository<Decorator> {
}