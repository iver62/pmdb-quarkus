package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoxOffice {

    private Long value;
    private String currency;

    public static BoxOffice of(Long value, String currency) {
        return
                BoxOffice.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
