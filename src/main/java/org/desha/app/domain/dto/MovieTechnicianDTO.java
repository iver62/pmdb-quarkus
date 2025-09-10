package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations sur un technicien associé à un film")
public class MovieTechnicianDTO {

    @Schema(description = "Identifiant unique de l'enregistrement", type = SchemaType.NUMBER, examples = "1")
    protected Long id;

    @NotNull(message = "Le film associé est obligatoire")
    @Schema(description = "Film associé au technicien", type = SchemaType.OBJECT, required = true)
    protected LiteMovieDTO movie;

    @NotNull(message = "La personne associée est obligatoire")
    @Schema(description = "Personne associée au rôle technique", type = SchemaType.OBJECT, required = true)
    protected LitePersonDTO person;

    @Schema(description = "Rôle du technicien dans le film", type = SchemaType.STRING, examples = "Réalisateur")
    protected String role;

    public static MovieTechnicianDTO build(Long id, LitePersonDTO personDTO, String role) {
        return
                MovieActorDTO.builder()
                        .id(id)
                        .person(personDTO)
                        .role(role)
                        .build()
                ;
    }

    public String toString() {
        return String.format("%d / %s: %s -> %s", id, person.getId(), person.getName(), role);
    }

}
