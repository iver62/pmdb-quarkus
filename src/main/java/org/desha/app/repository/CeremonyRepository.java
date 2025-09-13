package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.Ceremony;

import java.util.List;

@ApplicationScoped
public class CeremonyRepository implements PanacheRepositoryBase<Ceremony, Long> {

    /**
     * Compte le nombre de cérémonies correspondant à un terme donné.
     * <p>
     * Si le paramètre {@code term} est {@code null}, la méthode retourne le nombre total de cérémonies existantes.
     * Si un terme est fourni, elle compte uniquement les cérémonies dont le nom correspond (en ignorant les accents et la casse).
     * <p>
     *
     * @param term Le terme de recherche utilisé pour filtrer les cérémonies.
     *             Peut être {@code null} pour compter toutes les cérémonies.
     * @return Un {@link Uni} contenant le nombre de cérémonies correspondant au critère.
     */
    public Uni<Long> countCeremonies(String term) {
        return count("LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                Parameters.with("term", "%" + StringUtils.defaultString(term) + "%")
        );
    }

    /**
     * Récupère une liste paginée et triée de cérémonies correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche s'effectue sur le champ {@code name} et ignore la casse et les accents grâce à la fonction SQL 'unaccent'.
     * Si {@code term} est {@code null}, toutes les cérémonies sont retournées.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les cérémonies par nom. Peut être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link Ceremony} correspondant aux critères fournis.
     */
    public Uni<List<Ceremony>> findCeremonies(Page page, Sort.Direction direction, String term) {
        return find("""
                        SELECT c
                        FROM Ceremony c
                        WHERE LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """,
                Sort.by("name", direction),
                Parameters.with("term", "%" + StringUtils.defaultString(term) + "%")
        )
                .page(page)
                .list();
    }

}
