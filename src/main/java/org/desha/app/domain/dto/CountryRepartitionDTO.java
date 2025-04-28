package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.CountryRepartition;

@Getter
@Builder
public class CountryRepartitionDTO {

    private CountryDTO country;
    private Long total;

    public static CountryRepartitionDTO fromEntity(CountryRepartition countryRepartition) {
        return
                CountryRepartitionDTO.builder()
                        .country(CountryDTO.fromEntity(countryRepartition.getCountry()))
                        .total(countryRepartition.getTotal())
                        .build()
                ;
    }
}
