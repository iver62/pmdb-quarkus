package org.desha.app.domain.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import org.desha.app.exception.InvalidDateException;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class MovieQueryParamsDTO extends QueryParamsDTO {

    @Parameter(
            name = "country",
            description = "Identifiants des pays associés aux films",
            in = ParameterIn.QUERY,
            example = "country=1&country=2"
    )
    @QueryParam("country")
    private List<Integer> countryIds;

    @Parameter(
            name = "category",
            description = "Identifiants des catégories associés aux films à filtrer",
            in = ParameterIn.QUERY,
            example = "category=1&category=2"
    )
    @QueryParam("category")
    private List<Integer> categoryIds;

    @Parameter(
            name = "user",
            description = "Identifiants des utilisateurs ayant soumis les films (UUID)",
            in = ParameterIn.QUERY,
            example = "user=550e8400-e29b-41d4-a716-446655440000"
    )
    @QueryParam("user")
    private List<UUID> userIds;

    @Parameter(
            name = "from-release-date",
            description = "Filtrer les films sortis à partir de cette date (format ISO 8601)",
            in = ParameterIn.QUERY,
            example = "2000-01-01"
    )
    @QueryParam("from-release-date")
    private LocalDate fromReleaseDate;

    @Parameter(
            name = "to-release-date",
            description = "Filtrer les films sortis jusqu'à cette cette date (format ISO 8601)",
            in = ParameterIn.QUERY,
            example = "2024-12-31"
    )
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
