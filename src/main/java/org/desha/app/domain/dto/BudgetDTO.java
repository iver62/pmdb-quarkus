package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BudgetDTO {

    private Long value;

    private String currency;

    public static BudgetDTO of(Long value, String currency) {
        return
                BudgetDTO.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
