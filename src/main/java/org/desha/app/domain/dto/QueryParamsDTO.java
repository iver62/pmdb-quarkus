package org.desha.app.domain.dto;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

@Getter
public class QueryParamsDTO {

    @QueryParam("page")
    @DefaultValue("0")
    private int pageIndex;

    @QueryParam("size")
    @DefaultValue("50")
    private int size;

    @QueryParam("sort")
    @DefaultValue("title")
    private String sort;

    @QueryParam("direction")
    @DefaultValue("Ascending")
    private String direction;

    @QueryParam("term")
    @DefaultValue("")
    private String term;

}
