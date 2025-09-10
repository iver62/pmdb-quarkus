package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class MovieActorDTO extends MovieTechnicianDTO {

    @Schema(description = "Classement d’apparition de l’acteur dans le casting", type = SchemaType.INTEGER, examples = "1")
    private Integer rank;

    public static MovieActorDTO build(Long id, LitePersonDTO personDTO, String role, Integer rank) {
        return
                MovieActorDTO.builder()
                        .id(id)
                        .person(personDTO)
                        .role(role)
                        .rank(rank)
                        .build()
                ;
    }

    @Override
    public String toString() {
        return String.format("%s / %s: %s -> %s (%s)", id, person.getId(), person.getName(), role, rank);
    }
}
