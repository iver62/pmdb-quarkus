package org.desha.app.domain.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import org.desha.app.exception.InvalidDateException;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MovieQueryParamsDTO extends QueryParamsDTO {

    @QueryParam("country")
    private List<Integer> countryIds;

    @QueryParam("category")
    private List<Integer> categoryIds;

    @QueryParam("user")
    private List<UUID> userIds;

    @QueryParam("from-release-date")
    private LocalDate fromReleaseDate;

    @QueryParam("to-release-date")
    private LocalDate toReleaseDate;

    public void isInvalidDateRange() {
        if (Objects.nonNull(fromReleaseDate) && Objects.nonNull(toReleaseDate) && fromReleaseDate.isAfter(toReleaseDate)) {
            throw new InvalidDateException(
                    "fromReleaseDate - toReleaseDate",
                    MessageFormat.format("La date de sortie de début ({0}) est après la date de fin ({1}).", fromReleaseDate, toReleaseDate)
            );
        }
        if (Objects.nonNull(fromCreationDate) && Objects.nonNull(toCreationDate) && fromCreationDate.isAfter(toCreationDate)) {
            throw new InvalidDateException(
                    "fromCreationDate - toCreationDate",
                    MessageFormat.format("La date de création de début ({0}) est après la date de fin ({1}).", fromCreationDate, toCreationDate)
            );
        }
        if (Objects.nonNull(fromLastUpdate) && Objects.nonNull(toLastUpdate) && fromLastUpdate.isAfter(toLastUpdate)) {
            throw new InvalidDateException(
                    "fromLastUpdate - toLastUpdate",
                    MessageFormat.format("La date de modification de début ({0}) est après la date de fin ({1}).", fromLastUpdate, toLastUpdate)
            );
        }
    }

}
