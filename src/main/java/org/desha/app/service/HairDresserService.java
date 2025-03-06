package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.HairDresser;
import org.desha.app.repository.CostumierRepository;
import org.desha.app.repository.HairDresserRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class HairDresserService extends PersonService<HairDresser> {

    @Inject
    public HairDresserService(
            CountryService countryService,
            MovieRepository movieRepository,
            HairDresserRepository hairDresserRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, hairDresserRepository, fileService);
    }

    public Uni<Long> countMovies(Long hairDresserId, String term) {
        return movieRepository.countMoviesByHairDresser(hairDresserId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long hairDresserId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByHairDresser(hairDresserId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<HairDresser> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
