package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class CriteriasDTO {

    private String term;
    private List<Integer> countryIds;
    private List<Integer> genreIds;
    private List<UUID> userIds;
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

    public static CriteriasDTO build(PersonFilterDTO personFilterDTO) {
        return
                CriteriasDTO.builder()
                        .term(personFilterDTO.getTerm())
                        .countryIds(personFilterDTO.getCountryIds())
                        .fromBirthDate(personFilterDTO.getFromBirthDate())
                        .toBirthDate(personFilterDTO.getToBirthDate())
                        .fromDeathDate(personFilterDTO.getFromDeathDate())
                        .toDeathDate(personFilterDTO.getToDeathDate())
                        .fromCreationDate(personFilterDTO.getFromCreationDate())
                        .toCreationDate(personFilterDTO.getToCreationDate())
                        .fromLastUpdate(personFilterDTO.getFromLastUpdate())
                        .toLastUpdate(personFilterDTO.getToLastUpdate())
                        .build();
    }

    public static CriteriasDTO build(MovieFilterDTO movieFilterDTO) {
        return
                CriteriasDTO.builder()
                        .term(movieFilterDTO.getTerm())
                        .countryIds(movieFilterDTO.getCountryIds())
                        .genreIds(movieFilterDTO.getGenreIds())
                        .userIds(movieFilterDTO.getUserIds())
                        .fromReleaseDate(movieFilterDTO.getFromReleaseDate())
                        .toReleaseDate(movieFilterDTO.getToReleaseDate())
                        .fromCreationDate(movieFilterDTO.getFromCreationDate())
                        .toCreationDate(movieFilterDTO.getToCreationDate())
                        .fromLastUpdate(movieFilterDTO.getFromLastUpdate())
                        .toLastUpdate(movieFilterDTO.getToLastUpdate())
                        .build();
    }

}
