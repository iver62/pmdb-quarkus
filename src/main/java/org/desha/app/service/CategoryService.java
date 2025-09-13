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
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.mapper.CategoryMapper;
import org.desha.app.mapper.MovieMapper;
import org.desha.app.repository.CategoryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.utils.Messages;
import org.hibernate.exception.ConstraintViolationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final MovieMapper movieMapper;
    private final CategoryRepository categoryRepository;
    private final MovieRepository movieRepository;

    @Inject
    public CategoryService(
            CategoryMapper categoryMapper,
            MovieMapper movieMapper,
            CategoryRepository categoryRepository,
            MovieRepository movieRepository
    ) {
        this.categoryMapper = categoryMapper;
        this.movieMapper = movieMapper;
        this.categoryRepository = categoryRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * Compte le nombre de catégories correspondant à un terme donné.
     * <p>
     * Si le paramètre {@code term} est {@code null}, la méthode retourne le nombre total de catégories existantes.
     * Si un terme est fourni, elle compte uniquement les catégories dont le nom correspond (en ignorant les accents et la casse).
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param term Le terme de recherche utilisé pour filtrer les catégories.
     *             Peut être {@code null} pour compter toutes les catégories.
     * @return Un {@link Uni} contenant le nombre de catégories correspondant au critère.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> count(String term) {
        return
                categoryRepository.countCategories(term)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des catégories", throwable);
                                    return new WebApplicationException("Erreur lors du comptage des catégories", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de films associés à une catégorie spécifique, avec un filtrage optionnel par terme.
     * <p>
     * Cette méthode interroge le dépôt de films pour déterminer combien de films appartiennent
     * à la catégorie identifiée par {@code id}. Si un {@code term} est fourni, le comptage est restreint
     * aux films dont certains champs correspondent à ce terme (selon la logique définie en base).
     * <p>
     * Si aucune catégorie correspondant à l'identifiant fourni n'est trouvée, une {@link NotFoundException}
     * est levée. En cas d'erreur inattendue, une {@link WebApplicationException} est levée avec
     * un statut HTTP 500.
     *
     * @param id   L'identifiant unique de la catégorie dont on veut compter les films.
     * @param term Un terme optionnel pour filtrer les films associés à la catégorie.
     *             Peut être {@code null} pour compter tous les films de la catégorie.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères.
     * @throws NotFoundException       si aucune catégorie n'existe pour l'identifiant fourni.
     * @throws WebApplicationException en cas d'erreur interne lors du comptage des films.
     */
    public Uni<Long> countMoviesByCategory(Long id, String term) {
        return
                movieRepository.countMoviesByCategory(id, term)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CATEGORY))
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors du comptage des films pour la catégorie {}", id, throwable);
                                    return new WebApplicationException("Erreur lors du comptage des films pour la catégorie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une catégorie par son identifiant et la convertit en {@link CategoryDTO}.
     * <p>
     * Cette méthode interroge le dépôt des catégories pour trouver l'entité correspondant à l'identifiant
     * fourni. Si la catégorie existe, elle est transformée en {@link CategoryDTO} via le mapper associé.
     * <p>
     * Si aucune catégorie n'est trouvée, une {@link NotFoundException} est levée. En cas d'erreur inattendue (hors cas métier),
     * une {@link WebApplicationException} avec un statut HTTP 500 est levée.
     *
     * @param id L'identifiant unique de la catégorie à rechercher.
     * @return Un {@link Uni} émettant le {@link CategoryDTO} correspondant si trouvé.
     * @throws NotFoundException       si aucune catégorie n'existe pour l'identifiant fourni.
     * @throws WebApplicationException en cas d'erreur interne lors de la récupération.
     */
    public Uni<CategoryDTO> getById(Long id) {
        return
                categoryRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CATEGORY))
                        .map(categoryMapper::toDTO)
                        .onFailure().transform(e -> {
                                    if (e instanceof WebApplicationException) {
                                        return e;
                                    }
                                    log.error("Erreur lors de la récupération de la catégorie {}: {}", id, e.getMessage());
                                    return new WebApplicationException("Erreur lors de la récupération de la catégorie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste complète des catégories disponibles.
     * <p>
     * Cette méthode interroge le dépôt pour retourner toutes les entités {@link Category} persistées en base de données,
     * sans appliquer de critère de filtrage ni de pagination.
     * <p>
     * En cas d'échec de l'opération, l'erreur est journalisée, et une {@link WebApplicationException} avec un code HTTP 500 est levée.
     *
     * @return Un {@link Uni} émettant une {@link List} de {@link Category} représentant toutes les catégories existantes.
     * @throws WebApplicationException si une erreur survient lors de la récupération.
     */
    public Uni<List<Category>> getAll() {
        return
                categoryRepository.listAll()
                        .onFailure().transform(e -> {
                                    if (e instanceof WebApplicationException) {
                                        return e;
                                    }
                                    log.error("Erreur lors de la récupération des catégories: {}", e.getMessage());
                                    return new WebApplicationException("Erreur lors de la récupération des catégories", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de catégories en fonction des critères fournis.
     * <p>
     * Cette méthode interroge le dépôt afin de retourner les catégories correspondant éventuellement à un terme de recherche.
     * Les résultats peuvent être paginés et triés selon les paramètres passés.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel. Sinon {@code null}, seules les catégories dont le nom contient ce terme seront incluses.
     * @return Un {@link Uni} émettant une {@link List} de {@link CategoryDTO} correspondant aux critères de recherche et de tri.
     */
    public Uni<List<CategoryDTO>> getCategories(Page page, String sort, Sort.Direction direction, String term) {
        return
                categoryRepository
                        .findCategories(page, sort, direction, term)
                        .map(categoryMapper::toDTOList)
                        .onFailure().transform(e -> {
                                    if (e instanceof WebApplicationException) {
                                        return e;
                                    }
                                    log.error("Erreur lors de la récupération des catégories: {}", e.getMessage());
                                    return new WebApplicationException("Erreur lors de la récupération des catégories", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère un ensemble de catégories correspondant aux identifiants fournis.
     * <p>
     * Cette méthode interroge le dépôt pour obtenir les catégories dont l'identifiant
     * figure dans la liste {@code ids} et retourne le résultat sous forme de {@link Set}.
     *
     * @param ids La liste des identifiants des catégories à récupérer.
     * @return Un {@link Uni} émettant un {@link Set} de {@link Category} correspondant aux identifiants.
     */
    public Uni<Set<Category>> getByIds(List<Long> ids) {
        return categoryRepository.findByIds(ids).map(HashSet::new);
    }

    /**
     * Récupère une liste paginée et triée de films appartenant à une catégorie spécifique, en fonction des critères fournis.
     * <p>
     * Cette méthode interroge le dépôt afin de retourner les films correspondant à la catégorie identifiée par {@code id}.
     * Les résultats peuvent être filtrés selon {@link CriteriaDTO}, paginés et triés selon les paramètres passés.
     *
     * @param id          L'identifiant de la catégorie dont on souhaite récupérer les films.
     * @param page        Les informations de pagination à appliquer (index et taille de page).
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param criteriaDTO Les critères optionnels de filtrage des films. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link MovieDTO} correspondant à la catégorie et aux critères fournis.
     * @throws NotFoundException       si la catégorie n'existe pas.
     * @throws WebApplicationException en cas d'erreur interne lors de la récupération des films.
     */
    public Uni<List<MovieDTO>> getMoviesByCategory(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository.findMoviesByCategory(id, page, sort, direction, criteriaDTO)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CATEGORY))
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
                                    log.error("Erreur lors de la récupération des films appartenant à la catégorie {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des films pour la catégorie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Crée une nouvelle catégorie à partir des informations fournies dans un {@link CategoryDTO}.
     * <p>
     * Cette méthode persiste l'entité en base de données dans le cadre d'une transaction. Elle gère les violations de contraintes
     * et les erreurs internes en les transformant en exceptions HTTP appropriées.
     *
     * @param categoryDTO Les données de la catégorie à créer.
     * @return Un {@link Uni} émettant la {@link Category} créée.
     * @throws WebApplicationException avec {@link Response.Status#CONFLICT} si les contraintes de validation sont violées ou si la catégorie existe déjà.
     * @throws WebApplicationException avec {@link Response.Status#INTERNAL_SERVER_ERROR} en cas d'erreur inattendue lors de la création de la catégorie.
     */
    public Uni<Category> create(CategoryDTO categoryDTO) {
        return
                Panache
                        .withTransaction(() -> {
                            Category category = categoryMapper.toEntity(categoryDTO);
                            return categoryRepository.persist(category);
                        })
                        .onFailure(ConstraintViolationException.class).transform(throwable -> {
                                    log.error("Contrainte violée lors de la création de la catégorie", throwable);
                                    return new WebApplicationException("Erreur, La catégorie existe déjà ou ne respecte pas les contraintes de validation", Response.Status.CONFLICT);
                                }
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la création de la catégorie", throwable);
                                    return new WebApplicationException("Erreur lors de la création de la catégorie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Met à jour une catégorie existante en modifiant son nom.
     * <p>
     * Cette méthode recherche une catégorie par son identifiant et met à jour son nom avec la valeur fournie dans {@link  CategoryDTO}.
     * Si aucune catégorie n'est trouvée, une exception est levée. L'opération est effectuée dans une transaction.
     *
     * @param id          L'identifiant de la catégorie à mettre à jour.
     * @param categoryDTO L'objet contenant les nouvelles données de la catégorie.
     * @return Un {@link Uni} contenant l'entité mise à jour.
     * @throws IllegalArgumentException si aucune catégorie n'est trouvée avec l'identifiant donné.
     */
    public Uni<CategoryDTO> update(Long id, CategoryDTO categoryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                categoryRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CATEGORY))
                                        .invoke(category -> category.updateCategory(categoryDTO))
                                        .call(category -> categoryRepository.flush())
                                        .map(categoryMapper::toDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour de la catégorie", throwable);
                                    return new WebApplicationException("Erreur lors de la mise à jour de la catégorie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime une catégorie par son identifiant.
     * <p>
     * Cette méthode recherche une catégorie par son identifiant et tente de la supprimer. Si aucune catégorie correspondante n'est trouvée,
     * une exception est levée. L'opération est effectuée dans une transaction.
     *
     * @param id L'identifiant de la catégorie à supprimer.
     * @return Un {@link Uni} contenant `true` si la suppression a réussi, `false` sinon.
     * @throws IllegalArgumentException si aucune catégorie n'est trouvée avec l'identifiant donné.
     */
    public Uni<Boolean> deleteCategory(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                categoryRepository.deleteById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CATEGORY))
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la suppression de la catégorie: {}", throwable.getMessage());
                                    return new WebApplicationException("Erreur lors de la suppression de la catégorie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }
}
