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
import org.desha.app.domain.entity.Producer;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.ProducerRepository;

import java.util.List;

@Slf4j
@Singleton
public class ProducerService extends PersonService<Producer> {

    @Inject
    public ProducerService(
            CountryService countryService,
            MovieRepository movieRepository,
            ProducerRepository producerRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, producerRepository, fileService);
    }

    public Uni<Long> countMovies(long producerId, FiltersDTO filtersDTO) {
        return movieRepository.countMoviesByProducer(producerId, filtersDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long actorId, int page, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        return
                movieRepository
                        .findMoviesByProducer(actorId, page, size, sort, direction, filtersDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Producer> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Producer.fromDTO(personDTO).persist());
    }

}
