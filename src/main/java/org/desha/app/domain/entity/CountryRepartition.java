package org.desha.app.domain.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CountryRepartition {

    private Country country;
    private Long total;

}
