package org.desha.app.domain;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Genre;

import java.util.Objects;

@Slf4j
public class AuditGenreListener {

    @PrePersist
    @PreUpdate
    @PreRemove
    private void beforeAnyUpdate(Genre genre) {
        if (Objects.isNull(genre.getId())) {
            log.info("[GENRE AUDIT] About to add the genre " + genre.getName());
        } else {
            log.info("[GENRE AUDIT] About to update/delete genre: " + genre.getId());
        }
    }

}
