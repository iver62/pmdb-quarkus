package org.desha.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.desha.app.domain.enums.PersonType;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Critères de recherche pour les films ou personnes")
public class CriteriaDTO {

    @Schema(description = "Terme de recherche libre", type = SchemaType.STRING, examples = "Inception")
    private String term;

    @Schema(description = "Liste des identifiants de pays", type = SchemaType.ARRAY, examples = "[1, 2, 3]")
    private List<Integer> countryIds;

    @Schema(description = "Liste des identifiants de catégories", type = SchemaType.ARRAY, examples = "[4, 5]")
    private List<Integer> categoryIds;

    @Schema(description = "Liste des identifiants d'utilisateurs", type = SchemaType.ARRAY, examples = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    private List<UUID> userIds;

    @Schema(description = "Ensemble des types de personnes", type = SchemaType.ARRAY, examples = "[\"ACTOR\", \"DIRECTOR\"]")
    private Set<PersonType> personTypes;

    @Schema(description = "Date de naissance minimale", type = SchemaType.STRING, format = "date", examples = "1980-01-01")
    private LocalDate fromBirthDate;

    @Schema(description = "Date de naissance maximale", type = SchemaType.STRING, format = "date", examples = "2000-12-31")
    private LocalDate toBirthDate;

    @Schema(description = "Date de décès minimale", type = SchemaType.STRING, format = "date", examples = "2010-01-01")
    private LocalDate fromDeathDate;

    @Schema(description = "Date de décès maximale", type = SchemaType.STRING, format = "date", examples = "2020-12-31")
    private LocalDate toDeathDate;

    @Schema(description = "Date de sortie minimale pour les films", type = SchemaType.STRING, format = "date", examples = "2000-01-01")
    private LocalDate fromReleaseDate;

    @Schema(description = "Date de sortie maximale pour les films", type = SchemaType.STRING, format = "date", examples = "2024-12-31")
    private LocalDate toReleaseDate;

    @Schema(description = "Date de création minimale de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2023-01-01T00:00:00")
    private LocalDateTime fromCreationDate;

    @Schema(description = "Date de création maximale de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2024-09-10T23:59:59")
    private LocalDateTime toCreationDate;

    @Schema(description = "Date de dernière mise à jour minimale de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2023-01-01T00:00:00")
    private LocalDateTime fromLastUpdate;

    @Schema(description = "Date de dernière mise à jour maximale de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2024-09-10T23:59:59")
    private LocalDateTime toLastUpdate;

    public static CriteriaDTO build(PersonQueryParamsDTO queryParamsDTO) {
        return
                CriteriaDTO.builder()
                        .term(queryParamsDTO.getTerm())
                        .countryIds(queryParamsDTO.getCountryIds())
                        .personTypes(queryParamsDTO.getPersonTypes())
                        .fromBirthDate(queryParamsDTO.getFromBirthDate())
                        .toBirthDate(queryParamsDTO.getToBirthDate())
                        .fromDeathDate(queryParamsDTO.getFromDeathDate())
                        .toDeathDate(queryParamsDTO.getToDeathDate())
                        .fromCreationDate(queryParamsDTO.getFromCreationDate())
                        .toCreationDate(queryParamsDTO.getToCreationDate())
                        .fromLastUpdate(queryParamsDTO.getFromLastUpdate())
                        .toLastUpdate(queryParamsDTO.getToLastUpdate())
                        .build();
    }

    public static CriteriaDTO build(PersonQueryParamsDTO queryParamsDTO, PersonType personType) {
        return
                CriteriaDTO.builder()
                        .term(queryParamsDTO.getTerm())
                        .countryIds(queryParamsDTO.getCountryIds())
                        .personTypes(Set.of(personType))
                        .fromBirthDate(queryParamsDTO.getFromBirthDate())
                        .toBirthDate(queryParamsDTO.getToBirthDate())
                        .fromDeathDate(queryParamsDTO.getFromDeathDate())
                        .toDeathDate(queryParamsDTO.getToDeathDate())
                        .fromCreationDate(queryParamsDTO.getFromCreationDate())
                        .toCreationDate(queryParamsDTO.getToCreationDate())
                        .fromLastUpdate(queryParamsDTO.getFromLastUpdate())
                        .toLastUpdate(queryParamsDTO.getToLastUpdate())
                        .build();
    }

    public static CriteriaDTO build(MovieQueryParamsDTO queryParamsDTO) {
        return
                CriteriaDTO.builder()
                        .term(queryParamsDTO.getTerm())
                        .countryIds(queryParamsDTO.getCountryIds())
                        .categoryIds(queryParamsDTO.getCategoryIds())
                        .userIds(queryParamsDTO.getUserIds())
                        .fromReleaseDate(queryParamsDTO.getFromReleaseDate())
                        .toReleaseDate(queryParamsDTO.getToReleaseDate())
                        .fromCreationDate(queryParamsDTO.getFromCreationDate())
                        .toCreationDate(queryParamsDTO.getToCreationDate())
                        .fromLastUpdate(queryParamsDTO.getFromLastUpdate())
                        .toLastUpdate(queryParamsDTO.getToLastUpdate())
                        .build();
    }

}
