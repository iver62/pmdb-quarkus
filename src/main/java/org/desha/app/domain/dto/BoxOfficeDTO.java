package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations sur le box-office d’un film")
public class BoxOfficeDTO {

    @NotNull(message = "Le montant du box-office ne peut pas être nul")
    @Schema(description = "Montant du box-office", type = SchemaType.NUMBER, required = true, examples = "100000000")
    private long value;

    @Schema(description = "Devise utilisée pour le montant du box-office", examples = "USD")
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
