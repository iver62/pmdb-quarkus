package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.Country;
import org.desha.app.domain.Genre;

@ApplicationScoped
public class CountryRepository implements PanacheRepository<Country> {


}
