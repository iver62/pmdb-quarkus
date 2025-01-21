package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CountryRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CountryService {

    private final CountryRepository countryRepository;

    @Inject
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Uni<List<Country>> getByIds(Set<Country> countrySet) {
        return
                countryRepository.findByIds(
                        Optional.ofNullable(countrySet).orElse(Collections.emptySet())
                                .stream()
                                .map(p -> p.id)
                                .toList()
                );
    }

    public Uni<Set<Movie>> getMovies(Country country) {
        return Mutiny.fetch(country.getMovies());
    }

    public Uni<Set<Person>> getPersons(Country country) {
        return Mutiny.fetch(country.getPersons());
    }

    public Uni<Country> removeMovie(Long countryId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(countryId)
                                        .onItem().ifNotNull()
                                        .call(country -> country.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Country> updateCountry(Long id, Country country) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(id)
                                        .onItem().ifNotNull().invoke(
                                                entity -> {
                                                    entity.setCode(country.getCode());
                                                    entity.setAlpha2(country.getAlpha2());
                                                    entity.setAlpha3(country.getAlpha3());
                                                    entity.setNomEnGb(country.getNomEnGb());
                                                    entity.setNomFrFr(country.getNomFrFr());
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
                        )
                ;
    }
}
