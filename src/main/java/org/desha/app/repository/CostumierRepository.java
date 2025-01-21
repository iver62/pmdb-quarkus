package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Costumier;

@ApplicationScoped
public class CostumierRepository extends PersonRepository<Costumier> {
}