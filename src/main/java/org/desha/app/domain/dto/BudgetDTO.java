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
@Schema(description = "Informations sur le budget d’un film")
public class BudgetDTO {

    @NotNull(message = "Le montant du budget ne peut pas être nul")
    @Schema(description = "Montant du budget du film", type = SchemaType.NUMBER, required = true, examples = "50000000")
    private long value;

    @Schema(description = "Devise utilisée pour le budget", examples = "USD")
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
