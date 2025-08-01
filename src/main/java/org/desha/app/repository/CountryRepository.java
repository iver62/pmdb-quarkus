package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Person;
import org.desha.app.helper.MovieRepositoryHelper;

import java.util.List;

@ApplicationScoped
@Slf4j
public class CountryRepository implements PanacheRepository<Country> {

    /**
     * Compte le nombre de pays dont le nom en français correspond partiellement au terme recherché.
     *
     * @param term Le terme à rechercher dans le nom des pays (insensible à la casse).
     * @return Un {@link Uni} contenant le nombre total de pays correspondant au critère de recherche.
     */
    public Uni<Long> countCountries(String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        final String query = String.format(
                "LOWER(FUNCTION('unaccent', %s)) LIKE LOWER(FUNCTION('unaccent', ?1))",
                field
        );

        return count(query, "%" + term + "%");
    }

    /**
     * Cmpte le nombre de pays uniques liés aux films et applique une recherche insensible aux accents
     * et à la casse sur le nom du pays.
     *
     * @param term Le terme de recherche à appliquer sur le nom du pays (insensible aux accents et à la casse).
     * @return Un {@link Uni} contenant le nombre de pays distincts correspondant au terme de recherche.
     */
    public Uni<Long> countCountriesInMovies(String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Movie m
                        JOIN m.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """.formatted(field),
                Parameters.with("term", "%" + term + "%")
        );
    }

    public Uni<Long> countPersonCountries(String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Person p
                        JOIN p.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """.formatted(field),
                Parameters.with("term", "%" + term + "%")
        );
    }

    public Uni<Long> countMovieCountriesByPerson(Person person, String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        return count("""
                            SELECT COUNT(DISTINCT c)
                            FROM Movie m
                            JOIN m.countries c
                            WHERE (%s)
                            AND LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """.formatted(MovieRepositoryHelper.buildExistsClause(person), field),
                Parameters.with("person", person).and("term", "%" + term + "%")
        );
    }

    public Uni<Long> countMovieCategoriesByPerson(Person person, String term) {
        return count("""
                            SELECT COUNT(DISTINCT c)
                            FROM Movie m
                            JOIN m.categories c
                            WHERE (%s)
                            AND LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """.formatted(MovieRepositoryHelper.buildExistsClause(person)),
                Parameters.with("person", person).and("term", "%" + term + "%")
        );
    }

    /**
     * Récupère une liste de pays en fonction de leurs identifiants.
     *
     * @param ids la liste des identifiants des pays à récupérer.
     * @return un {@link Uni<List<Country>>} contenant la liste des pays correspondant aux identifiants fournis.
     */
    public Uni<List<Country>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

    /**
     * Récupère une liste de pays dont le nom en français correspond au terme recherché.
     *
     * @param sort      Le champ par lequel trier les résultats.
     * @param direction La direction du tri (ascendante ou descendante).
     * @param term      Le terme de recherche à filtrer (insensible à la casse).
     * @return Une {@link Uni<List<Country>} contenant la liste des pays correspondant à la recherche.
     */
    public Uni<List<Country>> findCountries(String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', nomFrFr)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + term + "%")
                ).list();
    }

    public Uni<List<Country>> findCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        final String query = String.format(
                "LOWER(FUNCTION('unaccent', %s)) LIKE LOWER(FUNCTION('unaccent', :term))",
                field
        );

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + term + "%"))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findCountriesInMovies(Page page, String sort, Sort.Direction direction, String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        String query = """
                SELECT DISTINCT c
                FROM Movie m
                JOIN m.countries c
                WHERE LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                """.formatted(field);
        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + term + "%"))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findPersonCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        final String query = """
                SELECT DISTINCT c
                FROM Person p
                JOIN p.countries c
                WHERE LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                """.formatted(field);

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + term + "%"))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findMovieCountriesByPerson(Person person, Page page, String sort, Sort.Direction direction, String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? "nomEnGb" : "nomFrFr";

        final String query = """
                    SELECT DISTINCT c
                    FROM Movie m
                    JOIN m.countries c
                    WHERE (%s)
                    AND LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                """.formatted(MovieRepositoryHelper.buildExistsClause(person), field);

        Parameters parameters = Parameters.with("person", person)
                .and("term", "%" + StringUtils.defaultString(term + "%"));

        return
                find(query, Sort.by(sort, direction), parameters)
                        .page(page)
                        .list()
                ;
    }
}
