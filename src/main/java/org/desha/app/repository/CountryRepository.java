package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Person;
import org.desha.app.helper.MovieRepositoryHelper;

import java.util.List;

@ApplicationScoped
public class CountryRepository implements PanacheRepository<Country> {

    private static final String NOM_EN_GB = "nomEnGb";
    private static final String NOM_FR_FR = "nomFrFr";

    /**
     * Compte le nombre de pays correspondant à un terme donné et à une langue spécifiée.
     * <p>
     * Si {@code term} est {@code null}, la méthode retourne le nombre total de pays existants.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * <p>
     *
     * @param term Le terme de recherche utilisé pour filtrer les pays. Peut être {@code null} pour compter tous les pays.
     * @param lang La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} contenant le nombre de pays correspondant aux critères fournis.
     */
    public Uni<Long> countCountries(@Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        final String query = String.format(
                "LOWER(FUNCTION('unaccent', %s)) LIKE LOWER(FUNCTION('unaccent', ?1))",
                field
        );

        return count(query, "%" + StringUtils.defaultString(term) + "%");
    }

    /**
     * Compte le nombre de pays associés à au moins un film et correspondant éventuellement à un terme de recherche,
     * en tenant compte de la langue spécifiée.
     * <p>
     * Si {@code term} est {@code null}, tous les pays associés à des films sont comptés.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     *
     * @param term Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} contenant le nombre de pays correspondant aux critères et associés à au moins un film.
     */
    public Uni<Long> countCountriesInMovies(@Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        final String query = String.format("""
                SELECT COUNT(c.id)
                FROM Country c
                WHERE EXISTS (
                    SELECT 1
                    FROM Movie m
                    JOIN m.countries mc
                    WHERE mc.id = c.id
                        AND LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                )""", field
        );

        return count(query, Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"));
    }

    /**
     * Compte le nombre de pays associés à au moins une personne et correspondant éventuellement à un terme de recherche,
     * en tenant compte de la langue spécifiée.
     * <p>
     * Si {@code term} est {@code null}, tous les pays associés à des personnes sont comptés.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     *
     * @param term Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} contenant le nombre de pays correspondant aux critères et associés à au moins une personne.
     */
    public Uni<Long> countPersonCountries(@Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        final String query = String.format("""
                SELECT COUNT(c.id)
                FROM Country c
                WHERE EXISTS (
                    SELECT 1
                    FROM Person p
                    JOIN p.countries pc
                    WHERE pc.id = c.id
                        AND LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                )""", field
        );

        return count(query, Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"));
    }

    /**
     * Compte le nombre de pays associés aux films liés à une personne spécifique,
     * et correspondant éventuellement à un terme de recherche, en tenant compte de la langue spécifiée.
     * <p>
     * Si {@code term} est {@code null}, tous les pays associés aux films de la personne sont comptés.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     *
     * @param person La personne dont on souhaite considérer les films. Ne peut pas être {@code null}.
     * @param term   Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang   La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} contenant le nombre de pays correspondant aux critères et associés aux films de la personne.
     */
    public Uni<Long> countMovieCountriesByPerson(Person person, @Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        return count(String.format("""
                            SELECT COUNT(c.id)
                            FROM Country c
                            WHERE EXISTS (
                                SELECT 1
                                FROM Movie m
                                JOIN m.countries pc
                                WHERE (%s)
                                    AND pc.id = c.id
                                        AND LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                        )""", MovieRepositoryHelper.buildExistsClause(person), field
                ),
                Parameters.with("person", person).and("term", "%" + StringUtils.defaultString(term) + "%")
        );
    }

    /**
     * Récupère une liste de pays correspondant aux identifiants fournis.
     * <p>
     * Si la liste {@code ids} est vide ou {@code null}, la méthode retourne une liste vide.
     *
     * @param ids La liste des identifiants des pays à récupérer. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link Country} correspondant aux identifiants fournis.
     */
    public Uni<List<Country>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

    /**
     * Récupère une liste triée de pays correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche s'effectue sur le champ français ({@code nomFrFr}) et ignore la casse et les accents grâce à la fonction SQL 'unaccent'.
     * Si {@code term} est {@code null}, tous les pays sont retournés.
     *
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link Country} correspondant aux critères fournis.
     */
    public Uni<List<Country>> findCountries(String sort, Sort.Direction direction, @Nullable String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', nomFrFr)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + StringUtils.defaultString(term) + "%")
                ).list();
    }

    /**
     * Récupère une liste paginée et triée de pays correspondant éventuellement à un terme de recherche,
     * en tenant compte de la langue spécifiée.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * Si {@code term} est {@code null}, tous les pays sont retournés.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} émettant une {@link List} de {@link Country} correspondant aux critères fournis.
     */
    public Uni<List<Country>> findCountries(Page page, String sort, Sort.Direction direction, @Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        final String query = String.format(
                "LOWER(FUNCTION('unaccent', %s)) LIKE LOWER(FUNCTION('unaccent', :term))",
                field
        );

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"))
                        .page(page)
                        .list();
    }

    /**
     * Récupère une liste paginée et triée de pays associés à des films correspondant éventuellement à un terme de recherche et
     * tenant compte de la langue spécifiée.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * Si {@code term} est {@code null}, tous les pays associés à des films sont retournés.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} émettant une {@link List} de {@link Country} correspondant aux critères fournis.
     */
    public Uni<List<Country>> findCountriesInMovies(Page page, String sort, Sort.Direction direction, @Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        String query = String.format("""
                SELECT DISTINCT c
                FROM Movie m
                JOIN m.countries c
                WHERE LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                """, field
        );

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"))
                        .page(page)
                        .list()
                ;
    }

    /**
     * Récupère une liste paginée et triée de pays associés à des personnes correspondant éventuellement à un terme de recherche
     * et tenant compte de la langue spécifiée.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * Si {@code term} est {@code null}, tous les pays associés à des personnes sont retournés.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} émettant une {@link List} de {@link Country} correspondant aux critères fournis.
     */
    public Uni<List<Country>> findPersonCountries(Page page, String sort, Sort.Direction direction, @Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        final String query = String.format("""
                SELECT DISTINCT c
                FROM Person p
                JOIN p.countries c
                WHERE LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                """, field
        );

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + StringUtils.defaultString(term) + "%"))
                        .page(page)
                        .list()
                ;
    }

    /**
     * Récupère une liste paginée et triée de pays associés aux films liés à une personne spécifique correspondant éventuellement à un
     * terme de recherche et tenant compte de la langue spécifiée.
     * <p>
     * La recherche ignore la casse et les accents grâce à l'utilisation de la fonction SQL 'unaccent'.
     * Le champ utilisé pour la comparaison dépend de la langue fournie : {@code NOM_EN_GB} pour "en", {@code NOM_FR_FR} sinon.
     * Si {@code term} est {@code null}, tous les pays associés aux films de la personne sont retournés.
     *
     * @param person    La personne dont on souhaite considérer les films. Ne peut pas être {@code null}.
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      La langue utilisée pour la recherche ("en" pour anglais, autre pour français).
     * @return Un {@link Uni} émettant une {@link List} de {@link Country} correspondant aux critères fournis.
     */
    public Uni<List<Country>> findMovieCountriesByPerson(Person person, Page page, String sort, Sort.Direction direction, @Nullable String term, String lang) {
        final String field = "en".equalsIgnoreCase(lang) ? NOM_EN_GB : NOM_FR_FR;

        final String query = String.format("""
                    SELECT DISTINCT c
                    FROM Movie m
                    JOIN m.countries c
                    WHERE (%s)
                        AND LOWER(FUNCTION('unaccent', c.%s)) LIKE LOWER(FUNCTION('unaccent', :term))
                """, MovieRepositoryHelper.buildExistsClause(person), field
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
