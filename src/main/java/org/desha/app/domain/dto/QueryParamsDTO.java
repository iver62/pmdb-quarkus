package org.desha.app.domain.dto;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import org.desha.app.exception.InvalidSortException;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
public class QueryParamsDTO {

    @QueryParam("page")
    @DefaultValue("0")
    private int pageIndex;

    @QueryParam("size")
    @DefaultValue("50")
    private int size;

    @QueryParam("sort")
    private String sort;

    @QueryParam("direction")
    @DefaultValue("Ascending")
    private String direction;

    @QueryParam("term")
    @DefaultValue("")
    private String term;

    @QueryParam("from-creation-date")
    protected LocalDateTime fromCreationDate;

    @QueryParam("to-creation-date")
    protected LocalDateTime toCreationDate;

    @QueryParam("from-last-update")
    protected LocalDateTime fromLastUpdate;

    @QueryParam("to-last-update")
    protected LocalDateTime toLastUpdate;

    public void validateSortField(String sort, List<String> allowedSortFields) {
        if (!allowedSortFields.contains(sort)) {
            throw new InvalidSortException(MessageFormat.format("Le champ de tri \"{0}\" est invalide. Valeurs autorisées : {1}", sort, allowedSortFields));
        }
    }

    public Sort.Direction validateSortDirection(String direction) {
        return Arrays.stream(Sort.Direction.values())
                .filter(d -> d.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(Sort.Direction.Ascending); // Valeur par défaut si invalide
    }

}
