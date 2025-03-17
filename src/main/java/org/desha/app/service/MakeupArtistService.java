package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CriteriasDTO;
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

    public Uni<Long> countMovies(long makeupArtistId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByMakeupArtist(makeupArtistId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long makeupArtistId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByMakeupArtist(makeupArtistId, page, sort, direction, criteriasDTO)
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
