package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Représente une cérémonie")
public class CeremonyDTO {

    @Schema(description = "Identifiant unique de la cérémonie", example = "1")
    private Long id;

    @NotBlank(message = "Le nom de la cérémonie est obligatoire")
    @Schema(description = "Nom de la cérémonie", example = "Césars")
    private String name;

    public static CeremonyDTO build(Long id, String name) {
        return
                CeremonyDTO.builder()
                        .id(id)
                        .name(name)
                        .build()
                ;
    }
}
