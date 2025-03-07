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
import org.desha.app.repository.CostumierRepository;
import org.desha.app.repository.MovieRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Singleton
public class CostumierService extends PersonService<Costumier> {

    @Inject
    public CostumierService(
            CountryService countryService,
            MovieRepository movieRepository,
            CostumierRepository costumierRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, costumierRepository, fileService);
    }

    public Uni<Long> countMovies(long costumierId, FiltersDTO filtersDTO) {
        return movieRepository.countMoviesByCostumier(costumierId, filtersDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long costumierId, int page, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        return
                movieRepository
                        .findMoviesByCostumier(costumierId, page, size, sort, direction, filtersDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Costumier> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
