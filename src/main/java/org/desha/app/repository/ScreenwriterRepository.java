package org.desha.app.repository;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Screenwriter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ScreenwriterRepository extends PersonRepository<Screenwriter> {

    public Uni<Long> count(
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        StringBuilder query = new StringBuilder("FROM Screenwriter s WHERE LOWER(FUNCTION('unaccent', s.name)) LIKE LOWER(FUNCTION('unaccent', :term))");
        Parameters params = Parameters.with("term", "%" + term + "%");

        if (Objects.nonNull(fromBirthDate)) {
            query.append(" AND s.dateOfBirth >= :fromBirthDate");
            params.and("fromBirthDate", fromBirthDate);
        }

        if (Objects.nonNull(toBirthDate)) {
            query.append(" AND s.dateOfBirth <= :toBirthDate");
            params.and("toBirthDate", toBirthDate);
        }

        if (Objects.nonNull(fromDeathDate)) {
            query.append(" AND s.dateOfDeath >= :fromDeathDate");
            params.and("fromDeathDate", fromDeathDate);
        }

        if (Objects.nonNull(toDeathDate)) {
            query.append(" AND s.dateOfDeath <= :toDeathDate");
            params.and("toDeathDate", toDeathDate);
        }

        if (Objects.nonNull(fromCreationDate)) {
            query.append(" AND s.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", fromCreationDate);
        }

        if (Objects.nonNull(toCreationDate)) {
            query.append(" AND s.creationDate <= :toCreationDate");
            params.and("toCreationDate", toCreationDate);
        }

        if (Objects.nonNull(fromLastUpdate)) {
            query.append(" AND s.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", fromLastUpdate);
        }

        if (Objects.nonNull(toLastUpdate)) {
            query.append(" AND s.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", toLastUpdate);
        }

        if (Objects.nonNull(countryIds) && !countryIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM s.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", countryIds);
        }

        return count(query.toString(), params);
    }

    public Uni<List<Screenwriter>> find(
            int pageIndex,
            int size,
            String sort,
            Sort.Direction direction,
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        StringBuilder query = new StringBuilder("FROM Screenwriter s WHERE LOWER(FUNCTION('unaccent', s.name)) LIKE LOWER(FUNCTION('unaccent', :term))");
        Parameters params = Parameters.with("term", "%" + term + "%");

        if (Objects.nonNull(fromBirthDate)) {
            query.append(" AND s.dateOfBirth >= :fromBirthDate");
            params.and("fromBirthDate", fromBirthDate);
        }

        if (Objects.nonNull(toBirthDate)) {
            query.append(" AND s.dateOfBirth <= :toBirthDate");
            params.and("toBirthDate", toBirthDate);
        }

        if (Objects.nonNull(fromDeathDate)) {
            query.append(" AND s.dateOfDeath >= :fromDeathDate");
            params.and("fromDeathDate", fromDeathDate);
        }

        if (Objects.nonNull(toDeathDate)) {
            query.append(" AND s.dateOfDeath <= :toDeathDate");
            params.and("toDeathDate", toDeathDate);
        }

        if (Objects.nonNull(fromCreationDate)) {
            query.append(" AND s.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", fromCreationDate);
        }

        if (Objects.nonNull(toCreationDate)) {
            query.append(" AND s.creationDate <= :toCreationDate");
            params.and("toCreationDate", toCreationDate);
        }

        if (Objects.nonNull(fromLastUpdate)) {
            query.append(" AND s.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", fromLastUpdate);
        }

        if (Objects.nonNull(toLastUpdate)) {
            query.append(" AND s.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", toLastUpdate);
        }

        if (Objects.nonNull(countryIds) && !countryIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM s.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", countryIds);
        }

        return
                find(query.toString(), Sort.by(sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }
}
