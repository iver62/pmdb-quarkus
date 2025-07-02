package org.desha.app.utils;

import lombok.experimental.UtilityClass;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Country;

import java.util.Objects;
import java.util.Set;

@UtilityClass
public class Utils {

    public boolean categoriesEquals(Set<CategoryDTO> c1, Set<Category> c2) {
        if (Objects.isNull(c1) || Objects.isNull(c2)) return false;
        return
                c1.stream().map(CategoryDTO::getId).toList()
                        .equals(c2.stream().map(Category::getId).toList())
                ;
    }

    public boolean countriesEquals(Set<CountryDTO> c1, Set<Country> c2) {
        if (Objects.isNull(c1) || Objects.isNull(c2)) return false;
        return
                c1.stream().map(CountryDTO::getId).toList()
                        .equals(c2.stream().map(Country::getId).toList())
                ;
    }
}
