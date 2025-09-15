package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.exception.PhotoDeletionException;
import org.desha.app.mapper.*;
import org.desha.app.repository.*;
import org.desha.app.utils.Messages;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class PersonService implements PersonServiceInterface {

    private static final String PHOTOS_DIR = "photos/";

    private final AwardMapper awardMapper;
    private final CategoryMapper categoryMapper;
    private final CountryMapper countryMapper;
    private final MovieMapper movieMapper;
    private final MovieActorMapper movieActorMapper;
    private final PersonMapper personMapper;

    private final CountryService countryService;
    private final FileService fileService;
    private final StatsService statsService;

    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final PersonRepository personRepository;

    @Inject
    protected PersonService(
            AwardMapper awardMapper,
            CategoryMapper categoryMapper,
            CountryMapper countryMapper,
            MovieMapper movieMapper,
            MovieActorMapper movieActorMapper,
            PersonMapper personMapper,
            CountryService countryService,
            FileService fileService,
            CategoryRepository categoryRepository,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            PersonRepository personRepository,
            StatsService statsService
    ) {
        this.awardMapper = awardMapper;
        this.categoryMapper = categoryMapper;
        this.countryMapper = countryMapper;
        this.movieMapper = movieMapper;
        this.movieActorMapper = movieActorMapper;
        this.personMapper = personMapper;
        this.countryService = countryService;
        this.fileService = fileService;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.personRepository = personRepository;
        this.statsService = statsService;
    }

    /**
     * Compte le nombre de personnes correspondant aux critères fournis.
     * <p>
     * Si le {@link CriteriaDTO} contient un terme de recherche, seules les personnes dont le nom correspond
     * (en ignorant les accents et la casse) sont comptées. Sinon, toutes les personnes sont comptées.
     * <p>
     * La méthode utilise le repository {@link PersonRepository} pour effectuer le comptage.
     * En cas d’erreur lors de l’exécution, une {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param criteriaDTO Les critères de recherche et de filtrage utilisés pour le comptage.
     * @return Un {@link Uni} contenant le nombre de personnes correspondant aux critères.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> countPersons(CriteriaDTO criteriaDTO) {
        return
                personRepository.countPersons(criteriaDTO)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des personnes", throwable);
                                    return new WebApplicationException("Impossible de compter les personnes", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de films associés à une personne donnée, en appliquant éventuellement des critères de filtrage.
     * <p>
     * Si {@code criteriaDTO} contient un terme de recherche, seuls les films dont le titre correspond
     * (en ignorant les accents et la casse) sont comptés.
     * <p>
     * La méthode vérifie d’abord que la personne existe via {@link PersonRepository#findById(Object)}.
     * En cas d’absence, une {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur lors du comptage des films, une {@link WebApplicationException} est levée
     * avec un statut HTTP 500, sauf si l’exception est déjà une {@link WebApplicationException}.
     *
     * @param id          L’identifiant unique de la personne pour laquelle compter les films. Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage appliqués au comptage des films.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères pour la personne donnée.
     * @throws NotFoundException       si la personne avec l’ID fourni n’existe pas.
     * @throws WebApplicationException si une erreur survient lors du comptage des films.
     */
    public Uni<Long> countMoviesByPerson(@NotNull Long id, CriteriaDTO criteriaDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .chain(person -> movieRepository.countMoviesByPerson(person, criteriaDTO))
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors du comptage des films pour la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de compter les films pour cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de rôles (films dans lesquels la personne a joué) pour une personne donnée.
     * <p>
     * Cette méthode utilise {@link MovieActorRepository#countMovieActorsByActor(Long)} pour déterminer le nombre
     * de rôles associés à l’identifiant de la personne fourni.
     * <p>
     * En cas d’erreur lors du comptage des rôles, une {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant unique de la personne pour laquelle compter les rôles. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le nombre de rôles de la personne.
     * @throws WebApplicationException si une erreur survient lors du comptage des rôles.
     */
    public Uni<Long> countRolesByPerson(@NotNull Long id) {
        return
                movieActorRepository.countMovieActorsByActor(id)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des rôles pour la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Erreur lors du comptage des rôles", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de pays correspondant à un terme de recherche donné et à une langue spécifique.
     * <p>
     * Si le paramètre {@code term} est {@code null}, la méthode retourne le nombre total de pays existants.
     * La recherche est insensible aux accents et à la casse.
     * <p>
     * La langue {@code lang} permet de filtrer ou localiser le comptage selon la langue souhaitée.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une {@link WebApplicationException} est levée
     * avec un statut HTTP 500.
     *
     * @param term Le terme de recherche utilisé pour filtrer les pays. Peut être {@code null} pour compter tous les pays.
     * @param lang La langue utilisée pour la recherche/localisation.
     * @return Un {@link Uni} contenant le nombre de pays correspondant au critère.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> countCountries(String term, String lang) {
        return
                countryRepository.countPersonCountries(term, lang)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des pays", throwable);
                                    return new WebApplicationException("Erreur lors du comptage des pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de pays associés aux films dans lesquels une personne donnée a participé.
     * <p>
     * Cette méthode filtre les pays en fonction d’un terme de recherche optionnel ({@code term}) et d’une langue ({@code lang})
     * si nécessaire. Si la personne n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur technique lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id   L'identifiant unique de la personne dont on souhaite compter les pays.
     * @param term Terme de recherche pour filtrer les noms de pays. Peut être {@code null} ou vide.
     * @param lang Langue utilisée pour filtrer ou localiser les noms de pays. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de pays associés aux films de la personne.
     * @throws NotFoundException       Si aucune personne avec l’identifiant fourni n’est trouvée.
     * @throws WebApplicationException En cas d’erreur technique lors du comptage.
     */
    public Uni<Long> countMovieCountriesByPerson(@NotNull Long id, String term, String lang) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .flatMap(person -> countryRepository.countMovieCountriesByPerson(person, term, lang))
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors du comptage des pays associés aux films de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de compter les pays des films liés à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de catégories associées aux films dans lesquels une personne donnée a participé.
     * <p>
     * Cette méthode peut filtrer les catégories en fonction d’un terme de recherche optionnel ({@code term}).
     * Si la personne n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur technique lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id   L'identifiant unique de la personne dont on souhaite compter les catégories des films.
     * @param term Terme de recherche pour filtrer les noms de catégories. Peut être {@code null} ou vide.
     * @return Un {@link Uni} contenant le nombre de catégories associées aux films de la personne.
     * @throws NotFoundException       Si aucune personne avec l’identifiant fourni n’est trouvée.
     * @throws WebApplicationException En cas d’erreur technique lors du comptage.
     */
    public Uni<Long> countMovieCategoriesByPerson(@NotNull Long id, String term) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .flatMap(person -> categoryRepository.countMovieCategoriesByPerson(person, term))
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors du comptage des catégories associées aux films de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de compter les catégories des films liés à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère les informations détaillées d'une personne à partir de son identifiant.
     * <p>
     * La méthode récupère la personne depuis le repository, charge ses pays associés et mappe l'entité vers un {@link PersonDTO}.
     * <p>
     * Si la personne n'existe pas, une exception {@link NotFoundException} est levée.
     * En cas d'erreur lors de la récupération ou du mapping, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id L'identifiant unique de la personne à récupérer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le {@link PersonDTO} correspondant à la personne.
     * @throws NotFoundException       si aucune personne n'est trouvée avec l'identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération ou du mapping de la personne.
     */
    public Uni<PersonDTO> getById(@NotNull Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .call(person -> Mutiny.fetch(person.getCountries()).invoke(person::setCountries))
                        .map(personMapper::toDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des informations de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer les informations de la personne demandée", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une version allégée des informations d'une personne à partir de son identifiant.
     * <p>
     * La méthode récupère la personne depuis le repository et mappe l'entité vers un
     * {@link LitePersonDTO} contenant uniquement les informations essentielles.
     * <p>
     * Si la personne n'existe pas, une exception {@link NotFoundException} est levée.
     * En cas d'erreur lors de la récupération ou du mapping, une exception
     * {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id L'identifiant unique de la personne à récupérer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le {@link LitePersonDTO} correspondant à la personne.
     * @throws NotFoundException       si aucune personne n'est trouvée avec l'identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération ou du mapping de la personne.
     */
    public Uni<LitePersonDTO> getLightById(@NotNull Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .map(personMapper::toLiteDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des informations de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer les informations de la personne demandée", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste de personnes à partir d'une liste d'identifiants.
     * <p>
     * Si la liste d'identifiants est {@code null} ou vide, la méthode retourne une liste vide.
     * Sinon, elle interroge le repository pour récupérer toutes les personnes correspondantes aux identifiants fournis.
     *
     * @param ids La liste des identifiants des personnes à récupérer. Peut être {@code null} ou vide.
     * @return Un {@link Uni} contenant la liste des {@link Person} correspondant aux identifiants fournis.
     */
    public Uni<List<Person>> getByIds(List<Long> ids) {
        return personRepository.findByIds(ids);
    }

    /**
     * Récupère une liste paginée de personnes sous forme allégée ({@link LitePersonDTO}) selon des critères de recherche, de tri et de pagination.
     * <p>
     * Les informations retournées sont limitées à celles contenues dans {@link LitePersonDTO}, ce qui permet d’optimiser les performances
     * lorsque les détails complets des personnes ne sont pas nécessaires.
     * <p>
     * En cas d’erreur lors de la récupération des données, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param page        La page à récupérer (pagination).
     * @param sort        Le nom du champ à utiliser pour le tri.
     * @param direction   La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param criteriaDTO Les critères de filtrage et de recherche des personnes.
     * @return Un {@link Uni} contenant la liste paginée de {@link LitePersonDTO} correspondant aux critères.
     * @throws WebApplicationException si une erreur survient lors de la récupération des personnes.
     */
    public Uni<List<LitePersonDTO>> getLightPersons(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository
                        .findPersons(page, sort, direction, criteriaDTO)
                        .map(personMapper::toLiteDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération de la liste des personnes pour la page {}, tri {}, direction {}", page, sort, direction, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des personnes selon les critères fournis", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée de personnes ({@link PersonDTO}) avec le nombre de films et le nombre de récompenses associés,
     * selon des critères de recherche, de tri et de pagination.
     * <p>
     * Cette méthode permet de lister les personnes avec des informations statistiques sur leur participation à des films.
     * <p>
     * En cas d’erreur lors de la récupération des données, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param page        La page à récupérer (pagination).
     * @param sort        Le nom du champ à utiliser pour le tri.
     * @param direction   La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param criteriaDTO Les critères de filtrage et de recherche des personnes.
     * @return Un {@link Uni} contenant la liste paginée de {@link PersonDTO} correspondant aux critères.
     * @throws WebApplicationException si une erreur survient lors de la récupération des personnes.
     */
    public Uni<List<PersonDTO>> getPersons(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository
                        .findPersonsWithMoviesNumber(page, sort, direction, criteriaDTO)
                        .map(personMapper::toDTOWithNumbersList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération de la liste des personnes pour la page {}, tri {}, direction {}", page, sort, direction, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des personnes selon les critères fournis", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste des rôles (MovieActorDTO) d'un acteur donné.
     * <p>
     * La liste est paginée et peut être triée selon un champ et une direction spécifiés.
     * Les informations retournées ne contiennent pas les détails de la personne associée.
     * <p>
     * En cas d’erreur lors de la récupération des données, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id        L'identifiant unique de l'acteur dont on souhaite récupérer les rôles. Ne peut pas être {@code null}.
     * @param page      Les informations de pagination.
     * @param sort      Le champ utilisé pour le tri.
     * @param direction La direction du tri (ASC ou DESC).
     * @return Un {@link Uni} contenant la liste des {@link MovieActorDTO} correspondant aux rôles de l'acteur.
     * @throws WebApplicationException si une erreur survient lors de la récupération des rôles.
     */
    public Uni<List<MovieActorDTO>> getRoles(@NotNull Long id, Page page, String sort, Sort.Direction direction) {
        return
                movieActorRepository.findMovieActorsByActor(id, page, sort, direction)
                        .map(movieActorMapper::toDTOListWithoutPerson)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des rôles de l'acteur avec l'ID {} pour la page {}, tri {}, direction {}", id, page, sort, direction, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des rôles de l'acteur selon les critères fournis", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste complète de toutes les personnes présentes dans la base de données.
     * <p>
     * Chaque entité {@link Person} est transformée en {@link PersonDTO}.
     * <p>
     * En cas d'erreur lors de la récupération des données, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @return un {@link Uni} contenant la liste de tous les {@link PersonDTO}.
     * @throws WebApplicationException si une erreur survient lors de la récupération des personnes.
     */
    public Uni<List<PersonDTO>> getAll() {
        return
                personRepository.listAll()
                        .map(personMapper::toDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération de la liste complète des personnes", throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste complète des personnes", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste des films associés à une personne donnée, avec le nombre de récompenses pour chaque film.
     * <p>
     * La méthode utilise l'identifiant de la personne pour rechercher ses films et applique les critères de pagination,
     * de tri et de filtrage fournis via {@link Page}, {@link Sort.Direction} et {@link CriteriaDTO}.
     * <p>
     * En cas d'absence de la personne avec l'identifiant fourni, une exception {@link NotFoundException} est levée.
     * En cas d'erreur lors de la récupération des films, une exception {@link WebApplicationException} est levée
     * avec un statut HTTP 500.
     *
     * @param id          L'identifiant unique de la personne dont on souhaite récupérer les films. Ne peut pas être {@code null}.
     * @param page        Les informations de pagination de la requête.
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC).
     * @param criteriaDTO Les critères supplémentaires de filtrage des films.
     * @return Un {@link Uni} contenant la liste des {@link MovieDTO} correspondant à la personne et aux critères fournis.
     * @throws NotFoundException       Si aucune personne n'existe avec l'identifiant fourni.
     * @throws WebApplicationException En cas d'erreur lors de la récupération des films pour cette personne.
     */
    public Uni<List<MovieDTO>> getMoviesByPerson(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .chain(person ->
                                movieRepository
                                        .findMoviesByPerson(person, page, sort, direction, criteriaDTO)
                                        .map(movieMapper::toDTOWithAwardsNumberList)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des films associés à la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des films liés à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste des pays associés aux personnes, avec pagination, tri et filtrage par terme et langue.
     * <p>
     * Le paramètre {@code term} permet de filtrer les pays par nom (ignorant la casse et les accents).
     * La langue {@code lang} peut être utilisée pour récupérer les noms localisés si disponible.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param page      Le numéro de page et la taille de page pour la pagination.
     * @param sort      Le champ par lequel trier les résultats.
     * @param direction La direction du tri (ASC ou DESC).
     * @param term      Le terme de recherche pour filtrer les noms de pays.
     * @param lang      La langue utilisée pour les noms de pays.
     * @return Un {@link Uni} contenant la liste des {@link CountryDTO} correspondant aux critères.
     * @throws WebApplicationException si une erreur survient lors de la récupération des pays.
     */
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findPersonCountries(page, sort, direction, term, lang)
                        .map(countryMapper::toDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des pays associés aux personnes pour la page {}, tri {}, direction {}", page, sort, direction, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des pays associés aux personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste paginée des pays associés aux films d'une personne spécifique.
     * <p>
     * La méthode vérifie d'abord l'existence de la personne avec l'identifiant fourni. Si la personne n'existe pas,
     * une exception {@link NotFoundException} est levée.
     * <p>
     * Les résultats peuvent être filtrés par un terme de recherche et triés selon les paramètres fournis.
     * La méthode retourne les données sous forme de {@link CountryDTO}.
     * <p>
     * En cas d'erreur lors de l'exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id        L'identifiant unique de la personne dont on souhaite récupérer les pays des films.
     * @param page      Les informations de pagination (numéro de page, taille de page).
     * @param sort      Le nom du champ utilisé pour trier les résultats.
     * @param direction La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param term      Terme optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      Langue utilisée pour filtrer ou localiser les noms des pays.
     * @return Un {@link Uni} contenant la liste des {@link CountryDTO} correspondant aux films de la personne.
     * @throws NotFoundException       si aucune personne n'est trouvée avec l'identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des données.
     */
    public Uni<List<CountryDTO>> getMovieCountriesByPerson(@NotNull Long id, Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .flatMap(person ->
                                countryRepository.findMovieCountriesByPerson(person, page, sort, direction, term, lang)
                                        .map(countryMapper::toDTOList)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des pays associés aux films liés à la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des pays associés aux films de cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste paginée des catégories associées aux films d'une personne spécifique.
     * <p>
     * La méthode vérifie d'abord l'existence de la personne avec l'identifiant fourni.
     * Si la personne n'existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Les résultats peuvent être filtrés par un terme de recherche et triés selon les paramètres fournis.
     * La méthode retourne les données sous forme de {@link CategoryDTO}.
     * <p>
     * En cas d'erreur lors de l'exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id        L'identifiant unique de la personne dont on souhaite récupérer les catégories des films.
     * @param page      Les informations de pagination (numéro de page, taille de page).
     * @param sort      Le nom du champ utilisé pour trier les résultats.
     * @param direction La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param term      Terme optionnel pour filtrer les catégories par nom. Peut être {@code null}.
     * @return Un {@link Uni} contenant la liste des {@link CategoryDTO} correspondant aux films de la personne.
     * @throws NotFoundException       Si aucune personne n'est trouvée avec l'identifiant fourni.
     * @throws WebApplicationException Si une erreur survient lors de la récupération des données.
     */
    public Uni<List<CategoryDTO>> getMovieCategoriesByPerson(@NotNull Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .flatMap(person ->
                                categoryRepository.findMovieCategoriesByPerson(person, page, sort, direction, term)
                                        .map(categoryMapper::toDTOList)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des catégories associées aux films liés à la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des catégories associés aux films de cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère l'ensemble des récompenses d'une personne spécifique.
     * <p>
     * La méthode vérifie d'abord l'existence de la personne avec l'identifiant fourni.
     * Si la personne n'existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Chaque récompense de la personne est transformée en {@link AwardDTO} et regroupée par {@link CeremonyAwardsDTO}.
     * Le résultat final est un ensemble de {@link CeremonyAwardsDTO}, chaque instance contenant la liste des récompenses
     * correspondantes.
     *
     * @param id L'identifiant unique de la personne dont on souhaite récupérer les récompenses.
     * @return Un {@link Uni} contenant un {@link Set} de {@link CeremonyAwardsDTO} avec les récompenses associées.
     * @throws NotFoundException Si aucune personne n'est trouvée avec l'identifiant fourni.
     */
    public Uni<Set<CeremonyAwardsDTO>> getAwardsByPerson(@NotNull Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                        .flatMap(
                                person -> Mutiny.fetch(person.getAwards())
                                        .map(awardMapper::toDTOList)
                                        .map(awardDTOList -> {
                                            Map<CeremonyAwardsDTO, List<AwardDTO>> grouped = awardDTOList.stream()
                                                    .collect(Collectors.groupingBy(AwardDTO::getCeremonyAwards));

                                            // Pour chaque groupe, créer un CeremonyAwardsDTO avec la liste des awards
                                            return grouped.entrySet().stream()
                                                    .map(entry -> {
                                                        CeremonyAwardsDTO ca = entry.getKey();
                                                        ca.setAwards(entry.getValue());
                                                        return ca;
                                                    })
                                                    .collect(Collectors.toSet());
                                        })
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des récompenses de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer les récompenses liées à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la photo d'une personne à partir du nom de fichier fourni.
     * <p>
     * Si le paramètre {@code fileName} est {@code null} ou vide, la méthode retourne la photo par défaut.
     * Si le fichier spécifié n'existe pas, la méthode retourne également la photo par défaut.
     * <p>
     * Les accès aux fichiers sont réalisés via le {@link FileService}.
     *
     * @param fileName Le nom du fichier de la photo à récupérer. Peut être {@code null} ou vide.
     * @return Un {@link Uni} contenant le {@link File} correspondant à la photo demandée ou à la photo par défaut.
     */
    public Uni<File> getPhoto(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank()) {
            log.warn("Photo name is missing, returning default photo.");
            return fileService.getFile(PHOTOS_DIR, Person.DEFAULT_PHOTO);
        }

        return
                fileService.getFile(PHOTOS_DIR, fileName)
                        .onFailure(FileNotFoundException.class).recoverWithUni(() -> {
                                    log.warn("Photo {} not found, returning default photo.", fileName);
                                    return fileService.getFile(PHOTOS_DIR, Person.DEFAULT_PHOTO);
                                }
                        )
                ;
    }

    /**
     * Uploade une photo fournie via un {@link FileUpload} dans le répertoire des photos.
     * <p>
     * Si le fichier fourni est {@code null}, invalide ou vide, la méthode retourne le nom de la photo par défaut.
     * En cas d'échec de l'upload, la méthode récupère également le nom de la photo par défaut et logue l'erreur.
     * <p>
     * L'upload réel est délégué au {@link FileService}.
     *
     * @param file Le fichier à uploader. Peut être {@code null} ou invalide.
     * @return Un {@link Uni} contenant le nom du fichier uploadé, ou {@link Person#DEFAULT_PHOTO} si le fichier est invalide ou l'upload échoue.
     */
    private Uni<String> uploadPhoto(FileUpload file) {
        if (Objects.isNull(file) || Objects.isNull(file.uploadedFile()) || file.fileName().isBlank()) {
            log.warn("Invalid or missing file. Using default photo.");
            return Uni.createFrom().item(Person.DEFAULT_PHOTO);
        }

        return
                fileService.uploadFile(PHOTOS_DIR, file)
                        .onFailure().recoverWithItem(error -> {
                                    log.error("Photo upload failed: {}", error.getMessage());
                                    return Person.DEFAULT_PHOTO;
                                }
                        )
                ;
    }

    /**
     * Crée et persiste une nouvelle personne dans la base de données.
     * <p>
     * Les informations de la personne sont fournies via le paramètre {@code personDTO}. Si la création ou la persistance échoue,
     * une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param personDTO le DTO contenant les informations de la personne à créer.
     *                  Doit contenir au minimum le nom de la personne et ses types.
     * @return un {@link Uni} contenant le {@link PersonDTO} créé et persistant dans la base de données.
     * @throws WebApplicationException si une erreur survient lors de la création ou de la persistance de la personne dans la base de données.
     */
    public Uni<PersonDTO> save(PersonDTO personDTO) {
        return
                Panache.withTransaction(() ->
                                personRepository.persist(personMapper.toEntity(personDTO))
                                        .map(personMapper::toDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la création de la personne: {}", personDTO, throwable);
                                    return new WebApplicationException("Impossible de créer la personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Met à jour les informations d'une personne existante, y compris sa photo et les pays associés.
     * <p>
     * Si un nouveau fichier photo est fourni, il sera téléchargé et la photo précédente sera supprimée
     * (sauf si c'était la photo par défaut). Si aucun fichier n'est fourni mais que le nom de la photo
     * change, la photo par défaut sera réattribuée.
     * <p>
     * Les pays associés à la personne sont mis à jour en fonction de la liste fournie dans {@code personDTO}.
     * <p>
     * Cette méthode s'exécute dans une transaction. En cas d'erreur, la transaction est annulée et
     * une exception appropriée est levée.
     *
     * @param id        L'identifiant unique de la personne à mettre à jour. Ne peut pas être {@code null}.
     * @param file      Le fichier à utiliser comme photo de la personne. Peut être {@code null} si la photo ne change pas.
     * @param personDTO Les informations de la personne à mettre à jour. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le {@link PersonDTO} mis à jour.
     * @throws WebApplicationException si {@code personDTO} est {@code null} ou si une erreur survient lors de la mise à jour.
     * @throws NotFoundException       si aucune personne n'existe avec l'identifiant fourni.
     *
     *                                 <p>Comportement spécifique :
     *                                 <ul>
     *                                     <li>Si le fichier photo est invalide ou absent, la photo par défaut est utilisée.</li>
     *                                     <li>Les erreurs lors du téléchargement de la photo ou de la récupération des pays sont loguées, et la méthode continue
     *                                         avec la photo par défaut ou les pays récupérés partiellement.</li>
     *                                     <li>En cas d'erreur inattendue, une {@link WebApplicationException} avec statut 500 est levée.</li>
     *                                 </ul>
     */
    public Uni<PersonDTO> update(@NotNull Long id, FileUpload file, PersonDTO personDTO) {
        return
                Panache.withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                                        .call(person -> countryService.getByIds(personDTO.getCountries())
                                                .onFailure().invoke(error -> log.error("Échec de la récupération des pays associés à la personne avec l'ID {} : {}", id, error.getMessage()))
                                                .invoke(person::setCountries)
                                        )
                                        .invoke(person -> person.updatePerson(personDTO))
                                        .call(person -> {
                                            final String currentPhoto = person.getPhotoFileName();
                                            final String dtoPhoto = personDTO.getPhotoFileName();

                                            if (Objects.nonNull(file)) {
                                                return uploadPhoto(file)
                                                        .onFailure().invoke(error -> log.error("Échec du téléchargement de la photo pour la personne avec l'ID {} : {}", id, error.getMessage()))
                                                        .chain(uploadedFileName ->
                                                                deletePhotoIfExists(currentPhoto) // On supprime l'ancien fichier si ce n'est pas le fichier par défaut
                                                                        .replaceWith(uploadedFileName)
                                                        )
                                                        .invoke(person::setPhotoFileName);
                                            } else if (!Objects.equals(currentPhoto, dtoPhoto)) {
                                                // Pas de nouveau fichier, mais différence → on remet la photo par défaut
                                                return
                                                        deletePhotoIfExists(currentPhoto)
                                                                .invoke(() -> person.setPhotoFileName(Person.DEFAULT_PHOTO))
                                                        ;
                                            }
                                            // Aucun changement de photo
                                            return Uni.createFrom().item(person);
                                        })
                                        .map(personMapper::toDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de mettre à jour les informations de la personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime un fichier photo existant associé à une personne.
     * <p>
     * Si le nom de fichier est {@code null}, vide ou correspond à la photo par défaut de la personne, aucune action n'est effectuée.
     * <p>
     * En cas d'erreur lors de la suppression du fichier, une exception
     * {@link PhotoDeletionException} est levée et un message d'erreur explicite est enregistré dans les logs.
     *
     * @param fileName Le nom du fichier photo à supprimer. Peut être {@code null} ou vide, auquel cas la méthode ne fait rien.
     * @return Un {@link Uni} complété lorsque l'opération est terminée.
     * @throws PhotoDeletionException Si la suppression du fichier échoue.
     */
    public Uni<Void> deletePhotoIfExists(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank() || Objects.equals(fileName, Person.DEFAULT_PHOTO)) {
            return Uni.createFrom().voidItem();
        }

        return Uni.createFrom().item(() -> {
            try {
                fileService.deleteFile(PHOTOS_DIR, fileName);
                return null;
            } catch (IOException e) {
                log.error("Erreur lors de la suppression de la photo {}: {}", fileName, e.getMessage());
                throw new PhotoDeletionException("Erreur lors de la suppression de la photo " + fileName);
            }
        });
    }

    /**
     * Met à jour la liste des pays associés à une personne donnée.
     * <p>
     * La méthode récupère la personne par son identifiant {@code id}, puis remplace ses pays existants par ceux fournis
     * dans {@code countryDTOSet}. Les informations de mise à jour sont également actualisées avec la date et l'heure actuelles.
     * <p>
     * Si l'identifiant {@code id} ne correspond à aucune personne existante, une exception {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur lors de la mise à jour dans la base de données ou de récupération des entités
     * pays, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id            L'identifiant unique de la personne à mettre à jour. Ne peut pas être {@code null}.
     * @param countryDTOSet L'ensemble des pays à associer à la personne. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant l'ensemble des {@link CountryDTO} mis à jour pour la personne.
     * @throws NotFoundException       si aucune personne n'est trouvée pour l'identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la mise à jour des pays.
     */
    public Uni<Set<CountryDTO>> updateCountries(@NotNull Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                                        .chain(person ->
                                                countryService.getByIds(
                                                                countryDTOSet.stream()
                                                                        .map(CountryDTO::getId)
                                                                        .filter(Objects::nonNull)
                                                                        .toList()
                                                        )
                                                        .invoke(finalCountrySet -> {
                                                            person.setCountries(finalCountrySet);
                                                            person.setLastUpdate(LocalDateTime.now());
                                                        })
                                                        .replaceWith(person)
                                        )
                                        .flatMap(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour des pays associés à la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de mettre à jour la liste des pays associés à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Ajoute de nouveaux pays à la liste des pays associés à une personne existante.
     *
     * <p>Cette méthode :
     * <ul>
     *   <li>Recherche la personne correspondante à l'ID fourni.</li>
     *   <li>Récupère la liste actuelle de ses pays associés.</li>
     *   <li>Ajoute les nouveaux pays fournis dans {@code countryDTOSet} à cette liste.</li>
     *   <li>Persiste les modifications en base de données.</li>
     *   <li>Retourne l'ensemble mis à jour des pays associés sous forme de {@link CountryDTO}.</li>
     * </ul>
     *
     * @param id            l'identifiant unique de la personne
     * @param countryDTOSet l'ensemble des pays à ajouter, représentés par des {@link CountryDTO}
     * @return un {@link Uni} émettant l'ensemble des pays mis à jour associés à la personne
     * @throws NotFoundException        si la personne avec l'ID fourni n'existe pas
     * @throws IllegalStateException    si la liste des pays existante est introuvable ou nulle
     * @throws IllegalArgumentException si un ou plusieurs des pays fournis sont introuvables
     * @throws WebApplicationException  si une erreur interne survient lors de l'ajout des pays
     */
    public Uni<Set<CountryDTO>> addCountries(@NotNull Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                                        .flatMap(person ->
                                                Mutiny.fetch(person.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
                                                        .chain(countries ->
                                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                                        .invoke(person::addCountries)
                                                        )
                                                        .replaceWith(person)
                                        )
                                        .chain(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de l'ajout de pays à la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible d'ajouter les pays à la liste associée à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime un pays de la liste des pays associés à une personne.
     *
     * <p>Cette méthode exécute les étapes suivantes :
     * <ul>
     *   <li>Recherche la personne par son identifiant {@code personId}.</li>
     *   <li>Charge la collection des pays associés à cette personne.</li>
     *   <li>Supprime le pays correspondant à l'identifiant {@code countryId}.</li>
     *   <li>Persiste les modifications en base de données.</li>
     *   <li>Retourne l'ensemble mis à jour des pays associés, sous forme de {@link CountryDTO}.</li>
     * </ul>
     *
     * <p>En cas d'erreur :
     * <ul>
     *   <li>Une {@link jakarta.ws.rs.NotFoundException} est levée si la personne n'existe pas.</li>
     *   <li>Une {@link IllegalStateException} est levée si la liste des pays est nulle.</li>
     *   <li>Une {@link WebApplicationException} est levée en cas d'échec de la suppression ou d'erreur interne.</li>
     * </ul>
     *
     * @param personId  l'identifiant unique de la personne concernée (non nul)
     * @param countryId l'identifiant unique du pays à supprimer (non nul)
     * @return un {@link Uni} émettant l'ensemble mis à jour des {@link CountryDTO}
     */
    public Uni<Set<CountryDTO>> removeCountry(@NotNull Long personId, @NotNull Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                                        .flatMap(person ->
                                                Mutiny.fetch(person.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
                                                        .invoke(countries -> person.removeCountry(countryId))
                                                        .replaceWith(person)
                                        )
                                        .chain(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la suppression du pays {} associé à la personne avec l'ID {}", countryId, personId, throwable);
                                    return new WebApplicationException("Impossible de supprimer le pays de la liste associée à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime une personne identifiée par son ID, ainsi que ses données associées.
     * <p>
     * Cette opération :
     * <ul>
     *   <li>Recherche la personne dans la base de données ;</li>
     *   <li>Supprime l'entité si elle existe ;</li>
     *   <li>Met à jour les statistiques des acteurs ;</li>
     *   <li>Supprime la photo associée, si ce n'est pas la photo par défaut.</li>
     * </ul>
     *
     * @param id l'identifiant unique de la personne à supprimer (ne doit pas être {@code null}).
     * @return un {@link Uni} émettant {@code true} si la suppression a réussi.
     * @throws NotFoundException       si aucune personne n'est trouvée pour l'ID fourni.
     * @throws WebApplicationException si une erreur survient lors de la suppression de la personne ou de ses données associées.
     */
    public Uni<Boolean> deletePerson(@NotNull Long id) {
        return
                Panache.withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                                        .flatMap(person -> {
                                                    final String photoFileName = person.getPhotoFileName();
                                                    return
                                                            personRepository.delete(person).replaceWith(true)
                                                                    .call(aBoolean -> statsService.updateActorsStats())
                                                                    .call(aBoolean -> deletePhotoIfExists(photoFileName))
                                                            ;
                                                }
                                        )
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la suppression de la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de supprimer la personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime tous les pays associés à une personne donnée.
     * <p>
     * Cette méthode supprime uniquement les associations entre la personne et ses pays, sans supprimer la personne elle-même
     * ni les entités pays correspondantes. Si la personne n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * La suppression est exécutée dans une transaction, garantissant que les changements sont atomiques. En cas d’erreur
     * technique, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant unique de la personne dont on souhaite supprimer les pays.
     * @return Un {@link Uni} contenant {@code true} si la suppression des pays a été effectuée avec succès.
     * @throws NotFoundException       si aucune personne correspondant à l’ID fourni n’existe.
     * @throws WebApplicationException si une erreur survient lors de la suppression des pays.
     */
    public Uni<Boolean> clearCountries(@NotNull Long id) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_PERSON))
                                        .flatMap(person ->
                                                Mutiny.fetch(person.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
                                                        .invoke(countries -> person.clearCountries())
                                                        .replaceWith(person)
                                        )
                                        .chain(personRepository::persist)
                                        .map(t -> true)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la suppression des pays associés à la personne avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de supprimer les pays associés à cette personne", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Prépare et persiste une entité {@link Person} à partir d’un {@link LitePersonDTO}.
     * <p>
     * Si la personne existe déjà (recherche par identifiant), elle est simplement enrichie avec le type fourni et retournée.
     * Si la personne n’existe pas, une nouvelle entité {@link Person} est créée à partir des informations de base contenues dans le DTO
     * (nom et photo), puis persistée avant d’ajouter le type.
     * <p>
     * Cette méthode garantit que la personne est toujours associée au {@link PersonType} indiqué, qu’elle ait été nouvellement créée
     * ou trouvée en base.
     *
     * @param litePersonDTO Le DTO contenant les informations minimales de la personne (identifiant, nom, photo).
     * @param type          Le type de la personne (par exemple {@link PersonType#ACTOR}, {@link PersonType#DIRECTOR}, etc.) à ajouter.
     * @return Un {@link Uni} contenant l’entité {@link Person} persistée ou mise à jour.
     */
    public Uni<Person> prepareAndPersistPerson(LitePersonDTO litePersonDTO, PersonType type) {
        return
                personRepository.findById(litePersonDTO.getId())
                        .onItem().ifNull().switchTo(() ->
                                personRepository.persist(
                                        Person.build(
                                                litePersonDTO.getName(),
                                                litePersonDTO.getPhotoFileName()
                                        )
                                )
                        )
                        .invoke(person -> person.addType(type))
                ;
    }

    /**
     * Récupère et convertit l’ensemble des pays associés à une personne en objets {@link CountryDTO}.
     * <p>
     * Cette méthode utilise {@link Mutiny#fetch(Object)} afin de charger paresseusement la collection des pays liés à l’entité
     * {@link Person}. Si cette collection est {@code null}, une exception est levée.
     * <p>
     * Une fois les pays récupérés, ils sont transformés en un ensemble immuable de DTO via le {@link CountryMapper}.
     *
     * @param person L’entité {@link Person} dont on souhaite récupérer et mapper les pays.
     * @return Un {@link Uni} contenant un {@link Set} de {@link CountryDTO}.
     * @throws IllegalStateException si la collection de pays de la personne est {@code null}.
     */
    private Uni<Set<CountryDTO>> fetchAndMapCountries(Person person) {
        return
                Mutiny.fetch(person.getCountries())
                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
                        .map(countryMapper::toDTOSet)
                ;
    }

}
