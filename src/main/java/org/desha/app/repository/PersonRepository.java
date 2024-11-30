package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.Person;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {


}
