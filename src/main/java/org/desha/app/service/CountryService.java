package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CountryService {

    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;

    @Inject
    public CountryService(
            CountryRepository countryRepository,
            MovieRepository movieRepository
    ) {
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
    }

    public Uni<Long> countCountries(String term) {
        return countryRepository.countCountries(term);
    }

    /**
     * Compte le nombre de films associés à un pays donné, en fonction d'un terme de recherche dans le titre des films.
     *
     * @param countryId L'identifiant du pays.
     * @param term      Le terme de recherche dans le titre des films.
     * @return Un objet {@link Uni} contenant le nombre de films correspondant aux critères.
     */
    public Uni<Long> countMovies(Long countryId, String term) {
        return movieRepository.countMoviesByCountry(countryId, term);
    }

    /**
     * Compte le nombre de personnes (acteurs, producteurs, etc.) associés à un pays spécifique en fonction des critères fournis.
     * Cette méthode est générique et peut être utilisée pour différents types de personnes (comme Actor, Producer, etc.)
     * en utilisant les repositories correspondants.
     *
     * <p>Cette méthode appelle la méthode {@link PersonRepository#countByCountry} pour effectuer la requête
     * de comptage des personnes filtrées par pays et les critères fournis dans {@link CriteriasDTO}.</p>
     *
     * @param <T>          Le type d'entité qui représente la personne (par exemple, {@link org.desha.app.domain.entity.Actor} ou {@link org.desha.app.domain.entity.Producer}).
     * @param <R>          Le type du repository utilisé pour accéder aux entités (par exemple, {@link org.desha.app.repository.ActorRepository} ou {@link org.desha.app.repository.ProducerRepository}).
     * @param countryId    L'ID du pays pour lequel les personnes doivent être comptées.
     * @param criteriasDTO Un objet contenant des critères supplémentaires pour filtrer les résultats de la recherche comme un terme de recherche sur le nom.
     * @param repository   Le repository correspondant à l'entité de type {@code T} (par exemple,
     *                     {@link org.desha.app.repository.ActorRepository} ou {@link org.desha.app.repository.ProducerRepository}).
     * @return Un objet {@link Uni<Long>} représentant le nombre de personnes associées au pays spécifié,
     * selon les critères de recherche et de filtrage.
     */
    public <T extends Person, R extends PersonRepository<T>> Uni<Long> countPersonsByCountry(Long countryId, CriteriasDTO criteriasDTO, R repository, Class<T> entityClass) {
        return repository.countByCountry(entityClass, countryId, criteriasDTO);
    }

    public Uni<Country> getById(Long id) {
        return
                countryRepository.findById(id)
                        .onFailure().recoverWithNull()
                ;
    }

    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term ,String lang) {
        return
                countryRepository.findCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(String sort, Sort.Direction direction, String term) {
        return
                countryRepository
                        .findCountries(sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
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

    public Uni<CountryDTO> getFull(Long id) {
        return
                countryRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Pays non trouvé"))
                        .call(country -> Mutiny.fetch(country.getMovies()))
                        .call(country -> Mutiny.fetch(country.getActors()))
                        .call(country -> Mutiny.fetch(country.getProducers()))
                        .call(country -> Mutiny.fetch(country.getDirectors()))
                        .call(country -> Mutiny.fetch(country.getScreenwriters()))
                        .call(country -> Mutiny.fetch(country.getMusicians()))
                        .call(country -> Mutiny.fetch(country.getPhotographers()))
                        .call(country -> Mutiny.fetch(country.getCostumiers()))
                        .call(country -> Mutiny.fetch(country.getDecorators()))
                        .call(country -> Mutiny.fetch(country.getEditors()))
                        .call(country -> Mutiny.fetch(country.getCasters()))
                        .call(country -> Mutiny.fetch(country.getArtDirectors()))
                        .call(country -> Mutiny.fetch(country.getSoundEditors()))
                        .call(country -> Mutiny.fetch(country.getVisualEffectsSupervisors()))
                        .call(country -> Mutiny.fetch(country.getMakeupArtists()))
                        .call(country -> Mutiny.fetch(country.getHairDressers()))
                        .call(country -> Mutiny.fetch(country.getStuntmen()))
                        .map(CountryDTO::fromFullEntity)
                ;
    }

    public Uni<List<CountryDTO>> searchByName(String name) {
        return
                countryRepository.findByName(name.trim())
                        .onItem().ifNotNull()
                        .transform(tList ->
                                tList.stream()
                                        .map(CountryDTO::fromEntity)
                                        .toList()
                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Long id, String sort, Sort.Direction direction, String term) {
        return
                movieRepository.findMoviesByCountry(id, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, null, null, null))
                                        .toList()
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                movieRepository.findMoviesByCountry(id, page, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, null, null, null))
                                        .toList()
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
     * @param service      Le service spécifique au type de personne (ex: {@link ActorService}, {@link DirectorService}).
     * @param repository   Le repository correspondant au type de personne (ex: {@link org.desha.app.repository.ActorRepository}, {@link org.desha.app.repository.DirectorRepository}).
     * @param entityClass  La classe de l'entité concernée (ex: `{@link org.desha.app.domain.entity.Actor}, {@link org.desha.app.domain.entity.Director}).
     * @return Une instance de {@link Uni<List<PersonDTO>>} contenant la liste des personnes sous forme de DTOs.
     */
    public <T extends Person, S extends PersonService<T>, R extends PersonRepository<T>> Uni<List<PersonDTO>> getPersonsByCountry(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO, S service, R repository, Class<T> entityClass) {
        return
                repository.findByCountry(entityClass, id, page, sort, direction, criteriasDTO)
                        .map(service::fromPersonListEntity)
                ;
    }

    public Uni<Country> update(Long id, CountryDTO countryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(id)
                                        .onItem().ifNotNull().invoke(
                                                entity -> {
                                                    entity.setCode(countryDTO.getCode());
                                                    entity.setAlpha2(countryDTO.getAlpha2());
                                                    entity.setAlpha3(countryDTO.getAlpha3());
                                                    entity.setNomEnGb(countryDTO.getNomEnGb());
                                                    entity.setNomFrFr(countryDTO.getNomFrFr());
                                                }
                                        )
                        )
                ;
    }

    public Set<CountryDTO> fromCountrySetEntity(Set<Country> genreSet) {
        return
                genreSet
                        .stream()
                        .map(CountryDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }
}
