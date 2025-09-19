package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.CeremonyAwards;

import java.util.List;

@ApplicationScoped
public class CeremonyAwardsRepository implements PanacheRepository<CeremonyAwards> {

    /**
     * Récupère la liste des cérémonies et leurs récompenses associées pour un film donné.
     * <p>
     * Cette méthode effectue une requête JPQL pour récupérer toutes les entités {@link CeremonyAwards}
     * liées au film identifié par l’ID fourni, en incluant les récompenses associées ({@link org.desha.app.domain.entity.Award})
     * grâce à un <code>JOIN FETCH</code> afin d’éviter les problèmes de lazt loading.
     *
     * @param id L’identifiant unique du film dont on souhaite récupérer les cérémonies et récompenses.
     * @return Un {@link Uni} émettant la liste des {@link CeremonyAwards} associées au film.
     */
    public Uni<List<CeremonyAwards>> findCeremoniesAwardsByMovie(Long id) {
        return
                find("""
                                SELECT DISTINCT ca
                                FROM CeremonyAwards ca
                                JOIN FETCH ca.awards a
                                WHERE ca.movie.id = :id
                                """,
                        Parameters.with("id", id)
                ).list();
    }

}
