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
import org.desha.app.domain.entity.SoundEditor;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.SoundEditorRepository;

import java.util.List;

@Slf4j
@Singleton
public class SoundEditorService extends PersonService<SoundEditor> {

    @Inject
    public SoundEditorService(
            CountryService countryService,
            MovieRepository movieRepository,
            SoundEditorRepository soundEditorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, soundEditorRepository, fileService);
    }

    public Uni<Long> countMovies(long soundEditorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesBySoundEditor(soundEditorId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long costumierId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesBySoundEditor(costumierId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<SoundEditor> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> SoundEditor.fromDTO(personDTO).persist());
    }
}
