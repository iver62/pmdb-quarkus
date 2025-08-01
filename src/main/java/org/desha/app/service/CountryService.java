package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Country;
import org.desha.app.mapper.CountryMapper;
import org.desha.app.mapper.MovieMapper;
import org.desha.app.mapper.PersonMapper;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.desha.app.utils.Messages;

import java.util.*;

@ApplicationScoped
@Slf4j
public class CountryService {

    private final CountryMapper countryMapper;
    private final MovieMapper movieMapper;
    private final PersonMapper personMapper;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;

    @Inject
    public CountryService(
            CountryMapper countryMapper,
            MovieMapper movieMapper,
            PersonMapper personMapper,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            PersonRepository personRepository
    ) {
        this.countryMapper = countryMapper;
        this.movieMapper = movieMapper;
        this.personMapper = personMapper;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
    }

    public Uni<Long> countCountries(String term, String lang) {
        return
                countryRepository.countCountries(term, lang)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des pays: {}", throwable.getMessage());
                                    return new WebApplicationException("Erreur lors du comptage des pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de films associés à un pays donné, en fonction d'un terme de recherche dans le titre des films.
     *
     * @param countryId L'identifiant du pays.
     * @param term      Le terme de recherche dans le titre des films.
     * @return Un objet {@link Uni} contenant le nombre de films correspondant aux critères.
     */
    public Uni<Long> countMoviesByCountry(Long countryId, String term) {
        return
                movieRepository.countMoviesByCountry(countryId, term)
                        .onItem().ifNull().failWith(new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors du comptage des films pour le pays {}", countryId, throwable);
                                    return new WebApplicationException("Erreur lors du comptage des films pour le pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de personnes (acteurs, producteurs, etc.) associés à un pays spécifique en fonction des critères fournis.
     * Cette méthode est générique et peut être utilisée pour différents types de personnes (comme Actor, Producer, etc.)
     * en utilisant les repositories correspondants.
     *
     * <p>Cette méthode appelle la méthode {@link PersonRepository#countByCountry} pour effectuer la requête
     * de comptage des personnes filtrées par pays et les critères fournis dans {@link CriteriasDTO}.</p>
     *
     * @param countryId    L'ID du pays pour lequel les personnes doivent être comptées.
     * @param criteriasDTO Un objet contenant des critères supplémentaires pour filtrer les résultats de la recherche comme un terme de recherche sur le nom.
     * @return Un objet {@link Uni<Long>} représentant le nombre de personnes associées au pays spécifié,
     * selon les critères de recherche et de filtrage.
     */
    public Uni<Long> countPersonsByCountry(Long countryId, CriteriasDTO criteriasDTO) {
        return
                personRepository.countByCountry(countryId, criteriasDTO)
                        .onItem().ifNull().failWith(new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors du comptage des personnes pour le pays {}", countryId, throwable);
                                    return new WebApplicationException("Erreur lors du comptage des personnes pour le pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<CountryDTO> getById(Long id) {
        return
                countryRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.NOT_FOUND_COUNTRY))
                        .map(countryMapper::countryToCountryDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération du pays {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération du pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findCountries(page, sort, direction, term, lang)
                        .map(countryMapper::toDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des pays", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(String sort, Sort.Direction direction, String term) {
        return
                countryRepository.findCountries(sort, direction, term)
                        .map(countryMapper::toDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des pays", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<Set<Country>> getByIds(Set<CountryDTO> countries) {
        return
                countryRepository.findByIds(
                        Optional.ofNullable(countries).orElse(Collections.emptySet())
                                .stream()
                                .map(CountryDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<Set<Country>> getByIds(List<Long> ids) {
        return countryRepository.findByIds(ids).map(HashSet::new);
    }

    public Uni<List<MovieDTO>> getMoviesByCountry(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository.findMoviesByCountry(id, page, sort, direction, criteriasDTO)
                        .onItem().ifNull().failWith(new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                        .map(movieWithAwardsNumberList ->
                                movieWithAwardsNumberList
                                        .stream()
                                        .map(movieMapper::movieWithAwardsNumberToMovieDTO)
                                        .toList()

                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des films appartenant au pays {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste de personnes (acteurs, réalisateurs, producteurs, etc.) appartenant à un pays donné,
     * en appliquant des critères de pagination, de tri et de filtrage.
     *
     * @param id           L'identifiant du pays pour lequel récupérer les personnes.
     * @param page         L'objet de pagination définissant l'index et la taille des résultats.
     * @param sort         Le champ sur lequel effectuer le tri.
     * @param direction    La direction du tri (ASC ou DESC).
     * @param criteriasDTO Les critères de filtrage pour affiner la recherche.
     * @return Une instance de {@link Uni<List<PersonDTO>>} contenant la liste des personnes sous forme de DTOs.
     */
    public Uni<List<LitePersonDTO>> getPersonsByCountry(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository.findPersonsByCountry(id, page, sort, direction, criteriasDTO)
                        .onItem().ifNull().failWith(new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                        .map(personMapper::toLiteDTOList)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des personnes appartenant au pays {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des personnes", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<CountryDTO> update(Long id, CountryDTO countryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                                        .invoke(country -> {
                                            country.setCode(countryDTO.getCode());
                                            country.setAlpha2(countryDTO.getAlpha2());
                                            country.setAlpha3(countryDTO.getAlpha3());
                                            country.setNomEnGb(countryDTO.getNomEnGb());
                                            country.setNomFrFr(countryDTO.getNomFrFr());
                                        })
                                        .call(category -> countryRepository.flush())
                                        .map(countryMapper::countryToCountryDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour du pays", throwable);
                                    return new WebApplicationException("Erreur lors de la mise à jour deu pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }
}
