package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoxOfficeDTO {

    private Long value;
    private String currency;

    public static BoxOfficeDTO of(Long value, String currency) {
        return
                BoxOfficeDTO.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
