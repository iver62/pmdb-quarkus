package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Country;

import java.util.List;

@ApplicationScoped
public class CountryRepository implements PanacheRepository<Country> {

    /**
     * Compte le nombre de pays dont le nom en français correspond partiellement au terme recherché.
     *
     * @param term Le terme à rechercher dans le nom des pays (insensible à la casse).
     * @return Un {@link Uni} contenant le nombre total de pays correspondant au critère de recherche.
     */
    public Uni<Long> countCountries(String term) {
        return count("LOWER(FUNCTION('unaccent', nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))",
                Parameters.with("term", term)
        );
    }

    /**
     * Cmpte le nombre de pays uniques liés aux films et applique une recherche insensible aux accents
     * et à la casse sur le nom du pays.
     *
     * @param term Le terme de recherche à appliquer sur le nom du pays (insensible aux accents et à la casse).
     * @return Un {@link Uni} contenant le nombre de pays distincts correspondant au terme de recherche.
     */
    public Uni<Long> countCountriesInMovies(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Movie m
                        JOIN m.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countActorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Actor a
                        JOIN a.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countProducerCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Producer p
                        JOIN p.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countDirectorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Director d
                        JOIN d.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countScreenwriterCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Screenwriter s
                        JOIN s.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countMusicianCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Musician m
                        JOIN m.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countDecoratorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Decorator d
                        JOIN d.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countCostumierCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Costumier co
                        JOIN co.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countPhotographerCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Photographer p
                        JOIN p.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countEditorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Editor e
                        JOIN e.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countCasterCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Caster ca
                        JOIN ca.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countArtDirectorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM ArtDirector ad
                        JOIN ad.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countSoundEditorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM SoundEditor se
                        JOIN se.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countVisualEffectsSupervisorCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM VisualEffectsSupervisor ves
                        JOIN ves.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countMakeupArtistCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM MakeupArtist ma
                        JOIN ma.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countHairDresserCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM HairDresser hd
                        JOIN hd.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
        );
    }

    public Uni<Long> countStuntmanCountries(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Stuntman s
                        JOIN s.countries c
                        WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                        """,
                Parameters.with("term", term)
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
                        "LOWER(FUNCTION('unaccent', nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))",
                        Sort.by(sort, direction),
                        Parameters.with("term", term)
                ).list();
    }

    public Uni<List<Country>> findCountries(Page page, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))",
                        Sort.by(sort, direction),
                        Parameters.with("term", term)
                )
                        .page(page)
                        .list()
                ;
    }

    public Uni<List<Country>> findByName(String nomFrFr) {
        String query = "LOWER(FUNCTION('unaccent', nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))";

        return find(query, Sort.by("nomFrFr"), Parameters.with("term", nomFrFr.toLowerCase()))
                .list();
    }

    public Uni<List<Country>> findCountriesInMovies(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Movie m
                JOIN m.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;
        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findActorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Actor a
                JOIN a.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findProducerCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Producer p
                JOIN p.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findDirectorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Director d
                JOIN d.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findScreenwriterCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Screenwriter s
                JOIN s.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findMusicianCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Musician m
                JOIN m.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findDecoratorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Decorator d
                JOIN d.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findCostumierCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Costumier co
                JOIN co.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findPhotographerCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Photographer p
                JOIN p.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findEditorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Editor e
                JOIN e.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findCasterCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Caster ca
                JOIN ca.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findArtDirectorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM ArtDirector ad
                JOIN ad.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findSoundEditorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM SoundEditor se
                JOIN se.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findVisualEffectsSupervisorCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM VisualEffectsSupervisor ves
                JOIN ves.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findMakeupArtistCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM MakeupArtist ma
                JOIN ma.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findHairDresserCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM HairDresser hd
                JOIN hd.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

    public Uni<List<Country>> findStuntmanCountries(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Stuntman s
                JOIN s.countries c
                WHERE LOWER(FUNCTION('unaccent', c.nomFrFr)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return
                find(query, Sort.by(sort, direction), Parameters.with("term", term))
                        .page(page)
                        .list();
    }

}
