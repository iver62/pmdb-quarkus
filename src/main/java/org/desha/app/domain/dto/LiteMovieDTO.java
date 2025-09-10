package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations principales sur un film")
public class LiteMovieDTO {

    @Schema(description = "Identifiant unique du film", type = SchemaType.NUMBER, examples = "1")
    private Long id;

    @NotBlank(message = "Le titre du film ne peut pas être vide")
    @Schema(description = "Titre du film", type = SchemaType.STRING, required = true, examples = "Inception")
    private String title;

    @Schema(description = "Titre original du film", type = SchemaType.STRING, examples = "Inception")
    private String originalTitle;

    @Schema(description = "Date de sortie du film", type = SchemaType.STRING, format = "date", examples = "2010-07-16")
    private LocalDate releaseDate;

}
