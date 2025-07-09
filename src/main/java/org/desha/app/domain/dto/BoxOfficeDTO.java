package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoxOfficeDTO {

    private Long value;
    private String currency;

    public static BoxOfficeDTO build(Long value, String currency) {
        return
                BoxOfficeDTO.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
