package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations sur le box-office d’un film")
public record BoxOfficeDTO(
        @NotNull(message = "Le montant du box-office ne peut pas être nul")
        @Schema(description = "Montant du box-office", type = SchemaType.NUMBER, required = true, examples = "100000000") long value,
        @Schema(description = "Devise utilisée pour le montant du box-office", examples = "USD") String currency
) {

    public static BoxOfficeDTO build(Long value, String currency) {
        return
                BoxOfficeDTO.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
