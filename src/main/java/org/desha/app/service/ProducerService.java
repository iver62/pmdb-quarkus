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
import org.desha.app.domain.entity.Producer;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.ProducerRepository;

import java.util.List;

@Slf4j
@Singleton
public class ProducerService extends PersonService<Producer> {

    @Inject
    public ProducerService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            ProducerRepository producerRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, producerRepository, fileService);
    }

    public Uni<Long> countMovies(long producerId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByProducer(producerId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long actorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByProducer(actorId, page, sort, direction, criteriasDTO)
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
        return countryRepository.countProducerCountries(term);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term) {
        return
                countryRepository.findProducerCountries(page, sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Producer> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Producer.fromDTO(personDTO).persist());
    }

}
