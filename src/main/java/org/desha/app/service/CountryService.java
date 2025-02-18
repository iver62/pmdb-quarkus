package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.repository.CountryRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CountryService {

    private final CountryRepository countryRepository;

    @Inject
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Uni<Set<Country>> getByIds(Set<CountryDTO> countries) {
        return
                countryRepository.findByIds(
                        Optional.ofNullable(countries).orElse(Collections.emptySet())
                                .stream()
                                .map(CountryDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<CountryDTO> getFull(Long id) {
        return
                countryRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Pays non trouvé"))
                        .call(country -> Mutiny.fetch(country.getMovies()))
                        .call(country -> Mutiny.fetch(country.getActors()))
                        .call(country -> Mutiny.fetch(country.getProducers()))
                        .call(country -> Mutiny.fetch(country.getDirectors()))
                        .call(country -> Mutiny.fetch(country.getScreenwriters()))
                        .call(country -> Mutiny.fetch(country.getMusicians()))
                        .call(country -> Mutiny.fetch(country.getPhotographers()))
                        .call(country -> Mutiny.fetch(country.getCostumiers()))
                        .call(country -> Mutiny.fetch(country.getDecorators()))
                        .call(country -> Mutiny.fetch(country.getEditors()))
                        .call(country -> Mutiny.fetch(country.getCasters()))
                        .call(country -> Mutiny.fetch(country.getArtDirectors()))
                        .call(country -> Mutiny.fetch(country.getSoundEditors()))
                        .call(country -> Mutiny.fetch(country.getVisualEffectsSupervisors()))
                        .call(country -> Mutiny.fetch(country.getMakeupArtists()))
                        .call(country -> Mutiny.fetch(country.getHairDressers()))
                        .call(country -> Mutiny.fetch(country.getStuntmen()))
                        .map(CountryDTO::fromCountry)
                ;
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
}
