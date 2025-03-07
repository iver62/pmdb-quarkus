package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.repository.GenreRepository;
import org.desha.app.repository.MovieRepository;

import java.util.*;

@ApplicationScoped
public class GenreService {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    @Inject
    public GenreService(
            GenreRepository genreRepository,
            MovieRepository movieRepository
    ) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
    }

    public Uni<Long> countMovies(Long id, String term) {
        return movieRepository.countMoviesByGenre(id, term);
    }

    public Uni<List<GenreDTO>> getGenres(String sort, Sort.Direction direction, String term) {
        return
                genreRepository
                        .findGenres(sort, direction, term)
                        .map(
                                genreList ->
                                        genreList
                                                .stream()
                                                .map(GenreDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Set<Genre>> getByIds(Set<GenreDTO> genreSet) {
        return
                genreRepository.findByIds(
                        Optional.ofNullable(genreSet).orElse(Collections.emptySet())
                                .stream()
                                .map(GenreDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<List<MovieDTO>> getMovies(Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                movieRepository.findMoviesByGenre(id, page, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()

                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    public Uni<Genre> removeMovie(Long genreId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                genreRepository.findById(genreId)
                                        .onItem().ifNotNull()
                                        .call(genre -> genre.removeMovie(movieId))
                        )
                ;
    }
}
