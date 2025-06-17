package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.PersonType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
public class CriteriasDTO {

    private String term;
    private List<Integer> countryIds;
    private List<Integer> categoryIds;
    private List<UUID> userIds;
    private Set<PersonType> personTypes;
    private LocalDate fromBirthDate;
    private LocalDate toBirthDate;
    private LocalDate fromDeathDate;
    private LocalDate toDeathDate;
    private LocalDate fromReleaseDate;
    private LocalDate toReleaseDate;
    private LocalDateTime fromCreationDate;
    private LocalDateTime toCreationDate;
    private LocalDateTime fromLastUpdate;
    private LocalDateTime toLastUpdate;

    public static CriteriasDTO build(PersonQueryParamsDTO queryParamsDTO) {
        return
                CriteriasDTO.builder()
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

    public static CriteriasDTO build(PersonQueryParamsDTO queryParamsDTO, PersonType personType) {
        return
                CriteriasDTO.builder()
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

    public static CriteriasDTO build(MovieQueryParamsDTO queryParamsDTO) {
        return
                CriteriasDTO.builder()
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
