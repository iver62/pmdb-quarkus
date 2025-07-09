package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CountryDTO {

    private Long id;
    private int code;
    private String alpha2;
    private String alpha3;
    private String nomEnGb;
    private String nomFrFr;

    public static CountryDTO build(Long id, int code, String alpha2, String alpha3, String nomEnGb, String nomFrFr) {
        return
                CountryDTO.builder()
                        .id(id)
                        .code(code)
                        .alpha2(alpha2)
                        .alpha3(alpha3)
                        .nomEnGb(nomEnGb)
                        .nomFrFr(nomFrFr)
                        .build()
                ;
    }
}
