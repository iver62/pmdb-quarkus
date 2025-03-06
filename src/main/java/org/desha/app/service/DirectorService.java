package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Director;
import org.desha.app.repository.DirectorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class DirectorService extends PersonService<Director> {

    @Inject
    public DirectorService(
            CountryService countryService,
            MovieRepository movieRepository,
            DirectorRepository directorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, directorRepository, fileService);
    }

    public Uni<Long> countMovies(Long directorId, String term) {
        return movieRepository.countMoviesByDirector(directorId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long directorId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByDirector(directorId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Director> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Director.fromDTO(personDTO).persist());
    }

}
