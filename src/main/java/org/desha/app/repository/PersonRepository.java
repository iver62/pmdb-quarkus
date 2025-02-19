package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import org.desha.app.domain.entity.Person;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Dependent
public class PersonRepository<T extends Person> implements PanacheRepository<T> {

    public Uni<Long> count(String name) {
        return count("lower(name) like lower(?1)", MessageFormat.format("%{0}%", name));
    }

    public Uni<List<T>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

    public Uni<List<T>> find(int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                find("lower(name) like lower(?1)", Sort.by(sort, direction), MessageFormat.format("%{0}%", term))
                        .page(pageIndex, size)
                        .list()
                ;
    }
}
