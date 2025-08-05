package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Year;
import java.util.Set;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Représente une récompense")
public class AwardDTO {

    @Schema(description = "Identifiant unique de la récompense", type = SchemaType.NUMBER)
    private Long id;

    @JsonIgnore
    private CeremonyAwardsDTO ceremonyAwards;

    @NotBlank(message = "Le nom de la récompense est obligatoire")
    @Schema(description = "Nom de la récompense", example = "Meilleur acteur", required = true)
    private String name;

    @Schema(description = "Liste des personnes associées à la récompense")
    private Set<LitePersonDTO> persons;

    @Schema(description = "Année de la récompense", example = "2024")
    private Year year;

    public static AwardDTO build(Long id, String name, Set<LitePersonDTO> persons, Year year) {
        return
                AwardDTO.builder()
                        .id(id)
                        .name(name)
                        .persons(persons)
                        .year(year)
                        .build()
                ;
    }

    @Override
    public String toString() {
        return "AwardDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", ceremonyAwards=" + (ceremonyAwards != null ? ceremonyAwards : "null") +
                ", persons=" + (persons != null
                ? persons.stream()
                .map(p -> p.getId() + ":" + p.getName())
                .toList()
                : "[]") +
                '}';
    }
}
