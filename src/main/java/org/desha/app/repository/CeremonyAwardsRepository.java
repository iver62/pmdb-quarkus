package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.CeremonyAwards;

import java.util.List;

@ApplicationScoped
@Slf4j
public class CeremonyAwardsRepository implements PanacheRepository<CeremonyAwards> {

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

    public Uni<List<CeremonyAwards>> findCeremoniesAwardsByPerson(Long id) {
        return
                find("""
                                SELECT DISTINCT ca
                                FROM CeremonyAwards ca
                                JOIN FETCH ca.awards a
                                JOIN a.personSet p
                                WHERE p.id = :id
                                """,
                        Parameters.with("id", id)
                ).list();
    }

}
