package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.MakeupArtist;
import org.desha.app.repository.MakeupArtistRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class MakeupArtistService extends PersonService<MakeupArtist> {

    @Inject
    public MakeupArtistService(
            CountryService countryService,
            MovieRepository movieRepository,
            MakeupArtistRepository makeupArtistRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, makeupArtistRepository, fileService);
    }

    public Uni<Long> countMovies(Long makeupArtistId, String term) {
        return movieRepository.countMoviesByMakeupArtist(makeupArtistId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long makeupArtistId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByMakeupArtist(makeupArtistId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<MakeupArtist> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> MakeupArtist.fromDTO(personDTO).persist());
    }
}
