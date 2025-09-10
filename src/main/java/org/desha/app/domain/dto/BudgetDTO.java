package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations sur le budget d’un film")
public record BudgetDTO(
        @NotNull(message = "Le montant du budget ne peut pas être nul")
        @Schema(description = "Montant du budget du film", type = SchemaType.NUMBER, required = true, examples = "50000000") long value,
        @Schema(description = "Devise utilisée pour le budget", examples = "USD") String currency
) {

    public static BudgetDTO build(Long value, String currency) {
        return
                BudgetDTO.builder()
                        .value(value)
                        .currency(currency)
                        .build()
                ;
    }
}
