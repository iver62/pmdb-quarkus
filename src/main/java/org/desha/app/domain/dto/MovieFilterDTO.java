package org.desha.app.domain.dto;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class MovieFilterDTO {

    @QueryParam("page")
    @DefaultValue("0")
    private int pageIndex;

    @QueryParam("size")
    @DefaultValue("50")
    private int size;

    @QueryParam("sort")
    @DefaultValue("title")
    private String sort;

    @QueryParam("direction") @DefaultValue("Ascending")
    private  String direction;

    @QueryParam("term") @DefaultValue("")
    private String term;

    @QueryParam("country")
    private List<Integer> countryIds;

    @QueryParam("genre")
    private List<Integer> genreIds;

    @QueryParam("user")
    private List<UUID> userIds;

    @QueryParam("from-release-date")
    private LocalDate fromReleaseDate;

    @QueryParam("to-release-date")
    private LocalDate toReleaseDate;

    @QueryParam("from-creation-date")
    private LocalDateTime fromCreationDate;

    @QueryParam("to-creation-date")
    private LocalDateTime toCreationDate;

    @QueryParam("from-last-update")
    private LocalDateTime fromLastUpdate;

    @QueryParam("to-last-update")
    private LocalDateTime toLastUpdate;

}
