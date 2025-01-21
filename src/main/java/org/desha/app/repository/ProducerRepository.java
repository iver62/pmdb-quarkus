package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Producer;

@ApplicationScoped
public class ProducerRepository extends PersonRepository<Producer> {
}
