package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.SoundEditor;
import org.desha.app.repository.ArtDirectorRepository;
import org.desha.app.repository.SoundEditorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class SoundEditorService implements PersonServiceInterface<SoundEditor> {

    private final SoundEditorRepository soundEditorRepository;

    @Inject
    public SoundEditorService(SoundEditorRepository soundEditorRepository) {
        this.soundEditorRepository = soundEditorRepository;
    }

    @Override
    public Uni<SoundEditor> getOne(Long id) {
        return soundEditorRepository.findById(id);
    }

    @Override
    public Uni<Set<SoundEditor>> getByIds(Set<PersonDTO> persons) {
        return
                soundEditorRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<SoundEditor>> getAll() {
        return
                soundEditorRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(SoundEditor soundEditor) {
        return Mutiny.fetch(soundEditor.getMovies());
    }
}
