package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Musician;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.MusicianRepository;

import java.util.List;

@Slf4j
@Singleton
public class MusicianService extends PersonService<Musician> {

    @Inject
    public MusicianService(
            CountryService countryService,
            MovieRepository movieRepository,
            MusicianRepository musicianRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, musicianRepository, fileService);
    }

    public Uni<Long> countMovies(Long musicianId, String term) {
        return movieRepository.countMoviesByDirector(musicianId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long musicianId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByMusician(musicianId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Musician> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Musician.fromDTO(personDTO).persist());
    }
}
