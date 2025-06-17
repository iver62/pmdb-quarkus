package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Category;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public static CategoryDTO of(Category category) {
        return
                CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .creationDate(category.getCreationDate())
                        .lastUpdate(category.getLastUpdate())
                        .build();
    }

    public static Set<CategoryDTO> fromCategorySetEntity(Set<Category> categorySet) {
        return
                Optional.ofNullable(categorySet).orElse(Set.of())
                        .stream()
                        .map(CategoryDTO::of)
                        .collect(Collectors.toSet())
                ;
    }

    public String toString() {
        return id + ": " + name;
    }

}
