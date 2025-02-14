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

    public Uni<Country> getOne(Long id) {
        return countryRepository.findById(id);
    }

    public Uni<Set<Country>> getAll() {
        return countryRepository.listAll().map(HashSet::new);
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
                        .map(CountryDTO::fromCountry)
                ;
    }

    public Uni<Set<Movie>> getMovies(Country country) {
        return Mutiny.fetch(country.getMovies());
    }

    public Uni<Set<Producer>> getProducers(Country country) {
        return Mutiny.fetch(country.getProducers());
    }

    public Uni<Set<Director>> getDirectors(Country country) {
        return Mutiny.fetch(country.getDirectors());
    }

    public Uni<Set<Screenwriter>> getScreenwriters(Country country) {
        return Mutiny.fetch(country.getScreenwriters());
    }

    public Uni<Set<Musician>> getMusicians(Country country) {
        return Mutiny.fetch(country.getMusicians());
    }

    public Uni<Set<Photographer>> getPhotographers(Country country) {
        return Mutiny.fetch(country.getPhotographers());
    }

    public Uni<Set<Costumier>> getCostumiers(Country country) {
        return Mutiny.fetch(country.getCostumiers());
    }

    public Uni<Set<Decorator>> getDecorators(Country country) {
        return Mutiny.fetch(country.getDecorators());
    }

    public Uni<Set<Editor>> getEditors(Country country) {
        return Mutiny.fetch(country.getEditors());
    }

    public Uni<Set<Caster>> getCasters(Country country) {
        return Mutiny.fetch(country.getCasters());
    }

    public Uni<Set<ArtDirector>> getArtDirectors(Country country) {
        return Mutiny.fetch(country.getArtDirectors());
    }

    public Uni<Set<SoundEditor>> getSoundEditors(Country country) {
        return Mutiny.fetch(country.getSoundEditors());
    }

    public Uni<Set<VisualEffectsSupervisor>> getVisualEffectsSupervisors(Country country) {
        return Mutiny.fetch(country.getVisualEffectsSupervisors());
    }

    public Uni<Set<MakeupArtist>> getMakeupArtists(Country country) {
        return Mutiny.fetch(country.getMakeupArtists());
    }

    public Uni<Set<HairDresser>> getHairDressers(Country country) {
        return Mutiny.fetch(country.getHairDressers());
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
                                                }
                                        )
                        )
                ;
    }
}
