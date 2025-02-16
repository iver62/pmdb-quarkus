package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.repository.GenreRepository;

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

    public Uni<Set<Genre>> getByIds(Set<GenreDTO> genreSet) {
        return
                genreRepository.findByIds(
                        Optional.ofNullable(genreSet).orElse(Collections.emptySet())
                                .stream()
                                .map(GenreDTO::getId)
                                .toList()
                ).map(HashSet::new);
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
