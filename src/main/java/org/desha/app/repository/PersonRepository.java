package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class PersonRepository<T extends Person> implements PanacheRepository<T> {

    public Uni<Long> count(String term) {
        return count("lower(name) like lower(:term)", Parameters.with("term", "%" + term + "%"));
    }

    public abstract Uni<Long> count(
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
    );

    public Uni<List<T>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

    public abstract Uni<List<T>> find(
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
    );

    public Uni<List<T>> find(int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                find("lower(name) like lower(:term)",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + term + "%")
                )
                        .page(pageIndex, size)
                        .list()
                ;
    }
}
