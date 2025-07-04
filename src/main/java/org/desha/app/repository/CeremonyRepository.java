package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Ceremony;

import java.util.List;

@ApplicationScoped
public class CeremonyRepository implements PanacheRepositoryBase<Ceremony, Long> {

    public Uni<Long> countCeremonies(String term) {
        return count("LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                Parameters.with("term", "%" + term + "%")
        );
    }

    /**
     * Recherche les intitulés distincts des cérémonies correspondant à un terme donné, avec pagination et tri.
     * <p>
     * La recherche est insensible à la casse et aux accents grâce à l'utilisation de la fonction SQL {@code unaccent}.
     *
     * @param page      Les informations de pagination (index de page et taille).
     * @param direction La direction du tri (ascendant ou descendant) appliquée au champ {@code ceremony}.
     * @param term      Le terme de recherche à utiliser dans la clause {@code LIKE}.
     * @return Un {@code Uni} contenant la liste des intitulés de cérémonies correspondant aux critères.
     */
    public Uni<List<Ceremony>> findCeremonies(Page page, Sort.Direction direction, String term) {
        return find("""
                        SELECT c
                        FROM Ceremony c
                        WHERE LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """,
                Sort.by("name", direction),
                Parameters.with("term", "%" + term.toLowerCase() + "%")
        )
                .page(page)
                .list();
    }

}
