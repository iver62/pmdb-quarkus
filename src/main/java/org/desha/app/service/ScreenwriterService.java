package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Screenwriter;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.ScreenwriterRepository;

import java.util.List;

@Slf4j
@Singleton
public class ScreenwriterService extends PersonService<Screenwriter> {

    @Inject
    public ScreenwriterService(
            CountryService countryService,
            MovieRepository movieRepository,
            ScreenwriterRepository screenwriterRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, screenwriterRepository, fileService);
    }

    public Uni<Long> countMovies(Long screenwriterId, String term) {
        return movieRepository.countMoviesByScreenwriter(screenwriterId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long screenwriterId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByScreenwriter(screenwriterId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Screenwriter> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Screenwriter.fromDTO(personDTO).persist());
    }

}
