package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Représente une catégorie de film")
public class CategoryDTO {

    @Schema(description = "Identifiant unique de la catégorie", type = SchemaType.NUMBER)
    private Long id;

    @NotBlank(message = "Le nom de la catégorie ne peut pas être vide")
    @Schema(description = "Nom de la catégorie", required = true, type = SchemaType.STRING, examples = {"Comédie", "Action"})
    private String name;

    @Schema(description = "Date de création de la catégorie", type = SchemaType.STRING, examples = "2024-07-01T10:15:30")
    private LocalDateTime creationDate;

    @Schema(description = "Date de dernière mise à jour de la catégorie", type = SchemaType.STRING, examples = "2024-07-15T12:00:00")
    private LocalDateTime lastUpdate;

    public static CategoryDTO build(Long id, String name, LocalDateTime creationDate, LocalDateTime lastUpdate) {
        return
                CategoryDTO.builder()
                        .id(id)
                        .name(name)
                        .creationDate(creationDate)
                        .lastUpdate(lastUpdate)
                        .build()
                ;
    }

}
