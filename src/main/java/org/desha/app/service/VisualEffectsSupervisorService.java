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

    public Uni<Long> countMovies(long visualEffectsSupervisorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByVisualEffectsSupervisor(visualEffectsSupervisorId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long visualEffectsSupervisorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByVisualEffectsSupervisor(visualEffectsSupervisorId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                        .toList()
                        )
                ;
    }

    public Uni<VisualEffectsSupervisor> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
