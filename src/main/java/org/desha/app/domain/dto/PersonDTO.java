package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.enums.PersonType;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations détaillées sur une personne")
public class PersonDTO extends LitePersonDTO {

    @Schema(description = "Nombre de films auxquels la personne a participé", type = SchemaType.INTEGER, examples = "12")
    private Long numberOfMovies;

    @Schema(description = "Nombre de récompenses obtenues par la personne", type = SchemaType.INTEGER, examples = "4")
    private Long numberOfAwards;

    @Schema(description = "Types de la personne (ex : acteur, réalisateur)", type = SchemaType.ARRAY, examples = "[\"ACTOR\", \"DIRECTOR\"]")
    private Set<PersonType> types;

    @Schema(description = "Ensemble des films associés à la personne", type = SchemaType.ARRAY)
    private Set<MovieDTO> movies;

    @Schema(description = "Ensemble des pays associés à la personne", type = SchemaType.ARRAY)
    private Set<CountryDTO> countries;

    @Schema(description = "Ensemble des récompenses reçues par la personne", type = SchemaType.ARRAY)
    private Set<Award> awards;

    @Schema(description = "Date de création de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2023-01-01T12:00:00")
    private LocalDateTime creationDate;

    @Schema(description = "Date de dernière mise à jour de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2024-09-10T15:30:00")
    private LocalDateTime lastUpdate;

}
