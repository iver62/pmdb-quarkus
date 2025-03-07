package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class FiltersDTO {

    private String term;
    private List<Integer> countryIds;
    private List<Integer> genreIds;
    private LocalDate fromReleaseDate;
    private LocalDate toReleaseDate;
    private LocalDateTime fromCreationDate;
    private LocalDateTime toCreationDate;
    private LocalDateTime fromLastUpdate;
    private LocalDateTime toLastUpdate;

    public static FiltersDTO build(String term, List<Integer> countryIds, List<Integer> genreIds, LocalDate fromReleaseDate, LocalDate toReleaseDate, LocalDateTime fromCreationDate, LocalDateTime toCreationDate, LocalDateTime fromLastUpdate, LocalDateTime toLastUpdate) {
        return
                FiltersDTO.builder()
                        .term(term)
                        .countryIds(countryIds)
                        .genreIds(genreIds)
                        .fromReleaseDate(fromReleaseDate)
                        .toReleaseDate(toReleaseDate)
                        .fromCreationDate(fromCreationDate)
                        .toCreationDate(toCreationDate)
                        .fromLastUpdate(fromLastUpdate)
                        .toLastUpdate(toLastUpdate)
                        .build();
    }
}
