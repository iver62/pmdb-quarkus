package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Représente un pays")
public class CountryDTO {

    @Schema(description = "Identifiant unique du pays", example = "1")
    private Long id;

    @Schema(description = "Code numérique du pays selon la norme ISO 3166-1", example = "250")
    private int code;

    @Schema(description = "Code alpha-2 du pays selon la norme ISO 3166-1", example = "FR")
    private String alpha2;

    @Schema(description = "Code alpha-3 du pays selon la norme ISO 3166-1", example = "FRA")
    private String alpha3;

    @Schema(description = "Nom du pays en anglais (GB)", example = "France")
    private String nomEnGb;

    @Schema(description = "Nom du pays en français (FR)", example = "France")
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
