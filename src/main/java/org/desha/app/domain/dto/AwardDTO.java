package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Person;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwardDTO {

    private Long id;
    @JsonIgnore
    private CeremonyAwardsDTO ceremonyAwards;
    private String name;
    private Set<LightPersonDTO> persons;
    private Year year;

    public static AwardDTO build(Long id, CeremonyAwardsDTO ceremonyAwards, String name, Set<LightPersonDTO> persons, Year year) {
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

    public static AwardDTO of(Award award) {
        return
                AwardDTO.build(
                        award.getId(),
                        CeremonyAwardsDTO.of(award.getCeremonyAwards()),
                        award.getName(),
                        null,
                        award.getYear()
                );
    }

    public static AwardDTO of(Award award, Set<Person> personSet) {
        return
                AwardDTO.build(
                        award.getId(),
                        null,
                        award.getName(),
                        LightPersonDTO.fromEntitySet(personSet),
                        award.getYear()
                );
    }

    public static List<AwardDTO> fromEntityList(List<Award> awardList) {
        return
                Optional.ofNullable(awardList).orElse(List.of())
                        .stream()
                        .map(AwardDTO::of)
                        .toList()
                ;
    }

    public static List<AwardDTO> fromEntityListWithPersons(List<Award> awards) {
        return Optional.ofNullable(awards).orElse(List.of())
                .stream()
                .map(award -> AwardDTO.of(award, award.getPersonSet()))
                .toList();
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
