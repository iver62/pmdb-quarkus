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

    public Uni<Long> countMovies(long artDirectorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByArtDirector(artDirectorId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long artDirectorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByArtDirector(artDirectorId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                        .toList()
                        )
                ;
    }

    public Uni<ArtDirector> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> ArtDirector.fromDTO(personDTO).persist());
    }
}
