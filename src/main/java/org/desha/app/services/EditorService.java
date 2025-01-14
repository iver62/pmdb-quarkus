package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Editor;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.CostumierRepository;
import org.desha.app.repository.EditorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class EditorService implements PersonServiceInterface<Editor> {

    private final EditorRepository editorRepository;

    @Inject
    public EditorService(EditorRepository editorRepository) {
        this.editorRepository = editorRepository;
    }

    @Override
    public Uni<Editor> getOne(Long id) {
        return editorRepository.findById(id);
    }

    @Override
    public Uni<Set<Editor>> getByIds(Set<PersonDTO> persons) {
        return
                editorRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Editor>> getAll() {
        return
                editorRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Editor editor) {
        return Mutiny.fetch(editor.getMovies());
    }
}
