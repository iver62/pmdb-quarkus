package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.VisualEffectsSupervisor;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.VisualEffectsSupervisorRepository;

import java.util.List;

@Slf4j
@Singleton
public class VisualEffectsSupervisorService extends PersonService<VisualEffectsSupervisor> {

    @Inject
    public VisualEffectsSupervisorService(
            CountryService countryService,
            MovieRepository movieRepository,
            VisualEffectsSupervisorRepository visualEffectsSupervisorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, visualEffectsSupervisorRepository, fileService);
    }

    public Uni<Long> countMovies(Long visualEffectsSupervisorId, String term) {
        return movieRepository.countMoviesByVisualEffectsSupervisor(visualEffectsSupervisorId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long visualEffectsSupervisorId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByVisualEffectsSupervisor(visualEffectsSupervisorId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<VisualEffectsSupervisor> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
