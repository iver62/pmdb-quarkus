package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import org.desha.app.domain.entity.Person;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Dependent
public class PersonRepository<T extends Person> implements PanacheRepository<T> {

    public Uni<List<T>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

}
