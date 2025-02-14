package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.GenreRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class GenreService {

    private final GenreRepository genreRepository;

    @Inject
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Uni<Genre> getOne(Long id) {
        return genreRepository.findById(id);
    }

    public Uni<Set<Genre>> getAll() {
        return genreRepository.listAll().map(HashSet::new);
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

    public Uni<Set<Movie>> getMovies(Long id) {
        return getOne(id).chain(genre -> Mutiny.fetch(genre.getMovies()));
    }

    public Uni<Genre> createGenre(GenreDTO genreDTO) {
        return
                Uni.createFrom()
                        .item(Genre.build(StringUtils.capitalize(genreDTO.getName()), LocalDateTime.now()))
                        .chain(genre -> Panache.withTransaction(genre::persist))
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

    public Uni<Genre> updateGenre(Long id, Genre genre) {
        return
                Panache
                        .withTransaction(() ->
                                genreRepository.findById(id)
                                        .onItem().ifNotNull().invoke(
                                                entity -> {
                                                    entity.setName(genre.getName());
                                                }
                                        )
                        )
                ;
    }
}
