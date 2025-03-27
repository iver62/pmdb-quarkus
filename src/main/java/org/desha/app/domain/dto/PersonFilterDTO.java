package org.desha.app.domain.dto;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PersonFilterDTO {

    @QueryParam("page")
    @DefaultValue("0")
    private int pageIndex;

    @QueryParam("size")
    @DefaultValue("50")
    private int size;

    @QueryParam("sort")
    @DefaultValue("name")
    private String sort;

    @QueryParam("direction")
    @DefaultValue("Ascending")
    private String direction;

    @QueryParam("term")
    @DefaultValue("")
    private String term;

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
