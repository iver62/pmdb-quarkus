package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Budget {

    private Long value;
    private String currency;

    public static Budget of(Long value, String currency) {
        return
                Budget.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
