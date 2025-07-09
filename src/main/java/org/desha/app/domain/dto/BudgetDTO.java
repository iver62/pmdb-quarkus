package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetDTO {

    private Long value;
    private String currency;

    public static BudgetDTO build(Long value, String currency) {
        return
                BudgetDTO.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
