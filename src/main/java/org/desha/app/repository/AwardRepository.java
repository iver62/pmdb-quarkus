package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Award;

import java.util.List;

@ApplicationScoped
public class AwardRepository implements PanacheRepository<Award> {

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
    public Uni<List<String>> findCeremonies(Page page, Sort.Direction direction, String term) {
        return find("""
                        SELECT DISTINCT a.ceremony
                        FROM Award a
                        WHERE LOWER(FUNCTION('unaccent', ceremony)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """,
                Sort.by("ceremony", direction),
                Parameters.with("term", "%" + term + "%")
        )
                .page(page)
                .project(String.class)
                .list();
    }

}
