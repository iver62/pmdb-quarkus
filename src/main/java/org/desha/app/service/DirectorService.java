package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Director;
import org.desha.app.repository.DirectorRepository;
import org.desha.app.repository.MovieRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Filter;

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

    public Uni<Long> countMovies(long directorId, FiltersDTO filtersDTO) {
        return movieRepository.countMoviesByDirector(directorId, filtersDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long directorId, Page page, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        return
                movieRepository
                        .findMoviesByDirector(directorId, page, sort, direction, filtersDTO)
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
