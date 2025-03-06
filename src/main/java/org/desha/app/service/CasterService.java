package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Caster;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.repository.CasterRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class CasterService extends PersonService<Caster> {

    @Inject
    public CasterService(
            CountryService countryService,
            MovieRepository movieRepository,
            CasterRepository casterRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, casterRepository, fileService);
    }

    public Uni<Long> countMovies(Long casterId, String term) {
        return movieRepository.countMoviesByCaster(casterId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long casterId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByCaster(casterId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Caster> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
