package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.repository.ArtDirectorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class ArtDirectorService extends PersonService<ArtDirector> {

    @Inject
    public ArtDirectorService(
            CountryService countryService,
            MovieRepository movieRepository,
            ArtDirectorRepository artDirectorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, artDirectorRepository, fileService);
    }

    public Uni<Long> countMovies(Long artDirectorId, String term) {
        return movieRepository.countMoviesByArtDirector(artDirectorId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long artDirectorId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByArtDirector(artDirectorId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<ArtDirector> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> ArtDirector.fromDTO(personDTO).persist());
    }
}
