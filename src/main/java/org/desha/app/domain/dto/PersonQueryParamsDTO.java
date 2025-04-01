package org.desha.app.domain.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PersonQueryParamsDTO extends QueryParamsDTO {

    @QueryParam("country")
    private List<Integer> countryIds;

    @QueryParam("from-birth-date")
    private LocalDate fromBirthDate;

    @QueryParam("to-birth-date")
    private LocalDate toBirthDate;

    @QueryParam("from-death-date")
    private LocalDate fromDeathDate;

    @QueryParam("to-death-date")
    private LocalDate toDeathDate;

    @QueryParam("from-creation-date")
    private LocalDateTime fromCreationDate;

    @QueryParam("to-creation-date")
    private LocalDateTime toCreationDate;

    @QueryParam("from-last-update")
    private LocalDateTime fromLastUpdate;

    @QueryParam("to-last-update")
    private LocalDateTime toLastUpdate;

}
