package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Caster;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.repository.CasterRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class CasterService extends PersonService<Caster> {

    @Inject
    public CasterService(
            CountryService countryService,
            MovieRepository movieRepository,
            CasterRepository casterRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, casterRepository, fileService);
    }

    public Uni<Long> countMovies(long casterId, FiltersDTO filtersDTO) {
        return movieRepository.countMoviesByCaster(casterId, filtersDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long casterId, Page page, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        return
                movieRepository
                        .findMoviesByCaster(casterId, page, sort, direction, filtersDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Caster> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
