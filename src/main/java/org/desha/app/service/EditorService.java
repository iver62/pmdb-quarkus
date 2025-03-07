package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Editor;
import org.desha.app.repository.EditorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class EditorService extends PersonService<Editor> {

    @Inject
    public EditorService(
            CountryService countryService,
            MovieRepository movieRepository,
            EditorRepository editorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, editorRepository, fileService);
    }

    public Uni<Long> countMovies(long editorId, FiltersDTO filtersDTO) {
        return movieRepository.countMoviesByEditor(editorId, filtersDTO);
    }

    public Uni<List<MovieDTO>> getMovies(
            long editorId,
            int page,
            int size,
            String sort,
            Sort.Direction direction,
            FiltersDTO filtersDTO
    ) {
        return
                movieRepository
                        .findMoviesByEditor(editorId, page, size, sort, direction, filtersDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Editor> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
