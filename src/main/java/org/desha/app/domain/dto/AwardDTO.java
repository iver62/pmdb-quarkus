package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.Year;
import java.util.Set;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwardDTO {

    private Long id;
    @JsonIgnore
    private CeremonyAwardsDTO ceremonyAwards;
    @NotBlank(message = "Le nom de la récompense est obligatoire")
    private String name;
    private Set<LitePersonDTO> persons;
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

    public static AwardDTO build(Long id, CeremonyAwardsDTO ceremonyAwards, String name, Set<LitePersonDTO> persons, Year year) {
        return
                AwardDTO.builder()
                        .id(id)
                        .ceremonyAwards(ceremonyAwards)
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
