package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.domain.dto.MovieDTO;
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

    /**
     * Compte le nombre de pays correspondant à un terme donné et à une langue spécifiée.
     * <p>
     * Si le paramètre {@code term} est {@code null}, la méthode retourne le nombre total de pays existants.
     * Si un terme est fourni, elle compte uniquement les pays dont le nom correspond (en tenant compte de la langue
     * et en ignorant la casse).
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param term Le terme de recherche utilisé pour filtrer les pays. Peut être {@code null} pour compter tous les pays.
     * @param lang La langue utilisée pour la recherche. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de pays correspondant aux critères.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> countCountries(@Nullable String term, String lang) {
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
     * Compte le nombre de films associés à un pays donné, éventuellement filtrés par un terme de recherche.
     * <p>
     * Si le paramètre {@code term} est {@code null}, la méthode retourne le nombre total de films pour le pays spécifié.
     * Si un terme est fourni, elle compte uniquement les films dont le nom correspond au terme (en ignorant la casse et les accents si applicable).
     * <p>
     * Si aucun pays ne correspond à l’identifiant {@code countryId}, une exception {@link NotFoundException} est levée.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param countryId L’identifiant du pays dont on souhaite compter les films. Ne peut pas être {@code null}.
     * @param term      Un terme de recherche optionnel pour filtrer les films par nom. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères.
     * @throws NotFoundException       si aucun pays ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors du comptage des films.
     */
    public Uni<Long> countMoviesByCountry(@NotNull Long countryId, @Nullable String term) {
        return
                movieRepository.countMoviesByCountry(countryId, term)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_COUNTRY))
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
     * Compte le nombre de personnes associées à un pays donné, éventuellement filtrées par des critères spécifiques.
     * <p>
     * Si {@code criteriaDTO} est {@code null}, la méthode retourne le nombre total de personnes pour le pays spécifié.
     * Si des critères sont fournis, seuls les enregistrements correspondant aux critères sont comptés.
     * <p>
     * Si aucun pays ne correspond à l’identifiant {@code countryId}, une exception {@link NotFoundException} est levée.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param countryId   L’identifiant du pays dont on souhaite compter les personnes. Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères optionnels pour filtrer les personnes. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de personnes correspondant aux critères.
     * @throws NotFoundException       si aucun pays ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors du comptage des personnes.
     */
    public Uni<Long> countPersonsByCountry(@NotNull Long countryId, CriteriaDTO criteriaDTO) {
        return
                personRepository.countByCountry(countryId, criteriaDTO)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_COUNTRY))
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

    /**
     * Récupère un pays par son identifiant.
     * <p>
     * Si aucun pays ne correspond à l’identifiant {@code id}, une exception {@link IllegalArgumentException} est levée.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant du pays à récupérer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} émettant le {@link CountryDTO} correspondant à l’identifiant fourni.
     * @throws IllegalArgumentException si aucun pays ne correspond à l’identifiant fourni.
     * @throws WebApplicationException  si une erreur survient lors de la récupération du pays.
     */
    public Uni<CountryDTO> getById(@NotNull Long id) {
        return
                countryRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.NOT_FOUND_COUNTRY))
                        .map(countryMapper::toDTO)
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

    /**
     * Récupère une liste paginée et triée de pays, éventuellement filtrés par un terme et une langue.
     * <p>
     * Si le paramètre {@code term} est {@code null}, tous les pays sont retournés.
     * La recherche peut être filtrée par {@code lang} pour ne récupérer que les pays correspondant à la langue spécifiée.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      La langue utilisée pour filtrer les pays. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link CountryDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des pays.
     */
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

    /**
     * Récupère une liste triée de pays, éventuellement filtrés par un terme de recherche.
     * <p>
     * Si le paramètre {@code term} est {@code null}, tous les pays sont retournés.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link CountryDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des pays.
     */
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

    /**
     * Récupère un ensemble de pays correspondant aux identifiants fournis.
     * <p>
     * Si l’ensemble {@code countries} est {@code null} ou vide, la méthode retourne un ensemble vide.
     *
     * @param countries Un ensemble de {@link CountryDTO} dont les identifiants doivent être récupérés. Peut être {@code null}.
     * @return Un {@link Uni} émettant un {@link Set} de {@link Country} correspondant aux identifiants fournis.
     */
    public Uni<Set<Country>> getByIds(Set<CountryDTO> countries) {
        return
                countryRepository.findByIds(
                        Optional.ofNullable(countries).orElse(Collections.emptySet())
                                .stream()
                                .map(CountryDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    /**
     * Récupère un ensemble de pays correspondant aux identifiants fournis.
     * <p>
     * Si la liste {@code ids} est vide ou {@code null}, la méthode retourne un ensemble vide.
     *
     * @param ids La liste des identifiants des pays à récupérer. Peut être {@code null}.
     * @return Un {@link Uni} émettant un {@link Set} de {@link Country} correspondant aux identifiants fournis.
     */
    public Uni<Set<Country>> getByIds(List<Long> ids) {
        return countryRepository.findByIds(ids).map(HashSet::new);
    }

    /**
     * Récupère une liste paginée et triée de films appartenant à un pays spécifique, éventuellement filtrée selon des critères.
     * <p>
     * Si aucun film ne correspond au pays identifié par {@code id}, une exception {@link NotFoundException} est levée.
     * La liste résultante est transformée en {@link MovieDTO}.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id          L’identifiant du pays dont on souhaite récupérer les films. Ne peut pas être {@code null}.
     * @param page        Les informations de pagination à appliquer (index et taille de page).
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param criteriaDTO Les critères optionnels pour filtrer les films. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link MovieDTO} correspondant aux critères fournis.
     * @throws NotFoundException       si aucun film n’est trouvé pour le pays fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des films.
     */
    public Uni<List<MovieDTO>> getMoviesByCountry(@NotNull Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository.findMoviesByCountry(id, page, sort, direction, criteriaDTO)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                        .map(movieWithAwardsNumberList ->
                                movieWithAwardsNumberList
                                        .stream()
                                        .map(movieMapper::movieWithAwardsNumberToMovieDTO)
                                        .toList()

                        )
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des films appartenant au pays {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de personnes appartenant à un pays spécifique, éventuellement filtrée selon des critères.
     * <p>
     * Si aucune personne ne correspond au pays identifié par {@code id}, une exception {@link NotFoundException} est levée.
     * La liste résultante est transformée en {@link LitePersonDTO}.
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id          L’identifiant du pays dont on souhaite récupérer les personnes. Ne peut pas être {@code null}.
     * @param page        Les informations de pagination à appliquer (index et taille de page).
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param criteriaDTO Les critères optionnels pour filtrer les personnes. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link LitePersonDTO} correspondant aux critères fournis.
     * @throws NotFoundException       si aucune personne n’est trouvée pour le pays fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des personnes.
     */
    public Uni<List<LitePersonDTO>> getPersonsByCountry(@NotNull Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository.findPersonsByCountry(id, page, sort, direction, criteriaDTO)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_COUNTRY))
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

    /**
     * Met à jour un pays existant avec les informations fournies dans un {@link CountryDTO}.
     * <p>
     * Si aucun pays ne correspond à l’identifiant {@code id}, une exception {@link NotFoundException} est levée.
     * La méthode applique les modifications à l’entité existante et retourne le {@link CountryDTO} mis à jour.
     * En cas d’erreur lors de l’exécution de la transaction, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id         L’identifiant du pays à mettre à jour. Ne peut pas être {@code null}.
     * @param countryDTO Les données à appliquer pour la mise à jour du pays.
     * @return Un {@link Uni} émettant le {@link CountryDTO} mis à jour.
     * @throws NotFoundException       si aucun pays ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la mise à jour du pays.
     */
    public Uni<CountryDTO> update(@NotNull Long id, CountryDTO countryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_COUNTRY))
                                        .invoke(country -> country.updateCountry(countryDTO))
                                        .call(category -> countryRepository.flush())
                                        .map(countryMapper::toDTO)
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
