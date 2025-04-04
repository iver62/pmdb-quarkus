package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.SoundEditor;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.SoundEditorRepository;

import java.util.List;

@Slf4j
@Singleton
public class SoundEditorService extends PersonService<SoundEditor> {

    @Inject
    public SoundEditorService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            SoundEditorRepository soundEditorRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, soundEditorRepository, fileService);
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
                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                        .toList()
                        )
                ;
    }

    @Override
    public Uni<Long> countCountries(String term) {
        return countryRepository.countSoundEditorCountries(term);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term) {
        return
                countryRepository.findSoundEditorCountries(page, sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<SoundEditor> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> SoundEditor.fromDTO(personDTO).persist());
    }
}
