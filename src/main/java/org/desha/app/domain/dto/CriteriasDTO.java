package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class CriteriasDTO {

    private String term;
    private List<Integer> countryIds;
    private List<Integer> genreIds;
    private List<String> usernames;
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

    public static CriteriasDTO build(String term, List<Integer> countryIds, List<Integer> genreIds, List<String> usernames, LocalDate fromReleaseDate, LocalDate toReleaseDate, LocalDateTime fromCreationDate, LocalDateTime toCreationDate, LocalDateTime fromLastUpdate, LocalDateTime toLastUpdate) {
        return
                CriteriasDTO.builder()
                        .term(term)
                        .countryIds(countryIds)
                        .genreIds(genreIds)
                        .usernames(usernames)
                        .fromReleaseDate(fromReleaseDate)
                        .toReleaseDate(toReleaseDate)
                        .fromCreationDate(fromCreationDate)
                        .toCreationDate(toCreationDate)
                        .fromLastUpdate(fromLastUpdate)
                        .toLastUpdate(toLastUpdate)
                        .build();
    }

    public static CriteriasDTO build(String term, List<Integer> countryIds, LocalDate fromBirthDate, LocalDate toBirthDate, LocalDate fromDeathDate, LocalDate toDeathDate, LocalDateTime fromCreationDate, LocalDateTime toCreationDate, LocalDateTime fromLastUpdate, LocalDateTime toLastUpdate) {
        return
                CriteriasDTO.builder()
                        .term(term)
                        .countryIds(countryIds)
                        .fromBirthDate(fromBirthDate)
                        .toBirthDate(toBirthDate)
                        .fromDeathDate(fromDeathDate)
                        .toDeathDate(toDeathDate)
                        .fromCreationDate(fromCreationDate)
                        .toCreationDate(toCreationDate)
                        .fromLastUpdate(fromLastUpdate)
                        .toLastUpdate(toLastUpdate)
                        .build();
    }

}
