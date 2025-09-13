package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Person;
import org.desha.app.helper.MovieRepositoryHelper;

import java.util.List;

@ApplicationScoped
public class CategoryRepository implements PanacheRepository<Category> {

    /**
     * Compte le nombre de catégories dont le nom correspond éventuellement à un terme de recherche.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Si {@code term} est {@code null}, toutes les catégories sont comptées.
     *
     * @param term Un terme de recherche optionnel. Si {@code null}, toutes les catégories sont comptées.
     * @return Un {@link Uni} émettant le nombre de catégories correspondant au terme fourni.
     */
    public Uni<Long> countCategories(@Nullable String term) {
        return
                count(
                        "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Parameters.with("term", "%" + StringUtils.defaultString(term) + "%")
                );
    }

    /**
     * Compte le nombre de catégories associées à au moins un film et correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Si {@code term} est {@code null}, toutes les catégories associées à des films sont comptées.
     *
     * @param term Un terme de recherche optionnel. Si {@code null}, toutes les catégories associées à des films sont comptées.
     * @return Un {@link Uni} émettant le nombre de catégories correspondant au terme fourni et associées à au moins un film.
     */
    public Uni<Long> countCategoriesInMovies(@Nullable String term) {
        String query = """
                SELECT COUNT(c.id)
                FROM Category c
                WHERE EXISTS (
                    SELECT 1
                    FROM Movie m
                    JOIN m.categories mc
                    WHERE mc.id = c.id
                        AND LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                )
                """;

        return count(query, Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"));
    }

    /**
     * Compte le nombre de catégories associées aux films liés à une personne spécifique
     * et correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Seules les catégories de films correspondant aux critères de la personne fournie sont comptées.
     *
     * @param person La personne dont on souhaite considérer les films. Ne peut pas être {@code null}.
     * @param term   Un terme de recherche optionnel pour filtrer les catégories par nom.
     *               Si {@code null}, toutes les catégories associées aux films de la personne sont comptées.
     * @return Un {@link Uni} émettant le nombre de catégories correspondant au terme fourni et associées aux films de la personne.
     */
    public Uni<Long> countMovieCategoriesByPerson(@NotNull Person person, @Nullable String term) {
        String query = String.format("""
                    SELECT COUNT(c.id)
                    FROM Category c
                    WHERE EXISTS (
                        SELECT 1
                        FROM Movie m
                        JOIN m.categories mc
                        WHERE (%s)
                            AND mc.id = c.id
                                AND LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                )""", MovieRepositoryHelper.buildExistsClause(person)
        );

        return count(query, Parameters.with("person", person).and("term", "%" + StringUtils.defaultString(term) + "%"));
    }

    /**
     * Récupère une liste de catégories correspondant aux identifiants fournis.
     *
     * @param ids La liste des identifiants des catégories à récupérer.
     * @return Un {@link Uni} émettant une {@link List} de {@link Category} correspondant aux identifiants.
     */
    public Uni<List<Category>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

    /**
     * Récupère une liste paginée et triée de catégories correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel. Si {@code null}, toutes les catégories sont retournées.
     * @return Un {@link Uni} émettant une {@link List} de {@link Category} correspondant aux critères de recherche et de tri.
     */
    public Uni<List<Category>> findCategories(Page page, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + StringUtils.defaultString(term) + "%")
                )
                        .page(page)
                        .list();
    }

    /**
     * Récupère une liste paginée et triée de catégories associées à des films, correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel. Si {@code null}, toutes les catégories associées à des films sont retournées.
     * @return Un {@link Uni} émettant une {@link List} de {@link Category} correspondant aux critères de recherche et de tri.
     */
    public Uni<List<Category>> findCategoriesInMovies(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Movie m
                JOIN m.categories c
                WHERE LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """;
        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"))
                        .page(page)
                        .list();
    }

    /**
     * Récupère une liste paginée et triée de catégories de films associées à une personne spécifique,
     * correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Seules les catégories des films correspondant aux critères de la personne fournie sont retournées.
     *
     * @param person    La personne dont on souhaite récupérer les catégories de films.
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel. Si {@code null}, toutes les catégories associées aux films de la personne sont retournées.
     * @return Un {@link Uni} émettant une {@link List} de {@link Category} correspondant aux critères fournis.
     */
    public Uni<List<Category>> findMovieCategoriesByPerson(Person person, Page page, String sort, Sort.Direction direction, String term) {
        final String query = String.format("""
                    SELECT DISTINCT c
                    FROM Movie m
                    JOIN m.categories c
                    WHERE (%s)
                    AND LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """, MovieRepositoryHelper.buildExistsClause(person)
        );

        Parameters parameters = Parameters.with("person", person)
                .and("term", "%" + StringUtils.defaultString(term) + "%");

        return
                find(query, Sort.by(sort, direction), parameters)
                        .page(page)
                        .list()
                ;
    }
}
