package org.desha.app.domain;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Category;

import java.util.Objects;

@Slf4j
public class AuditCategoryListener {

    @PrePersist
    @PreUpdate
    @PreRemove
    private void beforeAnyUpdate(Category category) {
        if (Objects.isNull(category.getId())) {
            log.info("[CATEGORY AUDIT] About to add the category {}", category.getName());
        } else {
            log.info("[CATEGORY AUDIT] About to update/delete category: {}", category.getId());
        }
    }

}
