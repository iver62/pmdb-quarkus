package org.desha.app.mapper;

import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    CategoryDTO categoryToCategoryDTO(Category entity);

    @Named("toLiteCategoryDTO")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    CategoryDTO toLiteCategoryDTO(Category entity);

    List<CategoryDTO> toDTOList(List<Category> categoryList);

    Set<CategoryDTO> toDTOSet(Set<Category> categorySet);
}
