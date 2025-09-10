package org.desha.app.domain.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.exception.InvalidDateException;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public class PersonQueryParamsDTO extends QueryParamsDTO {

    @Parameter(
            name = "country",
            description = "Identifiants des pays associés à la personne (ex: country=1&country=2)",
            in = ParameterIn.QUERY,
            example = "1"
    )
    @QueryParam("country")
    private List<Integer> countryIds;

    @Parameter(
            name = "type",
            description = "Types de personnes à filtrer (ex: ACTOR, DIRECTOR)",
            in = ParameterIn.QUERY,
            example = "ACTOR"
    )
    @QueryParam("type")
    private Set<PersonType> personTypes;

    @Parameter(
            name = "from-birth-date",
            description = "Date de naissance minimale (format : yyyy-MM-dd)",
            in = ParameterIn.QUERY,
            example = "1950-01-01"
    )
    @QueryParam("from-birth-date")
    private LocalDate fromBirthDate;

    @Parameter(
            name = "to-birth-date",
            description = "Date de naissance maximale (format : yyyy-MM-dd)",
            in = ParameterIn.QUERY,
            example = "2000-12-31"
    )
    @QueryParam("to-birth-date")
    private LocalDate toBirthDate;

    @Parameter(
            name = "from-death-date",
            description = "Date de décès minimale (format : yyyy-MM-dd)",
            in = ParameterIn.QUERY,
            example = "1990-01-01"
    )
    @QueryParam("from-death-date")
    private LocalDate fromDeathDate;

    @Parameter(
            name = "to-death-date",
            description = "Date de décès maximale (format : yyyy-MM-dd)",
            in = ParameterIn.QUERY,
            example = "2023-12-31"
    )
    @QueryParam("to-death-date")
    private LocalDate toDeathDate;

    public void isInvalidDateRange() {
        if (Objects.nonNull(fromBirthDate) && Objects.nonNull(toBirthDate) && fromBirthDate.isAfter(toBirthDate)) {
            throw new InvalidDateException(
                    "fromBirthDate - toBirthDate",
                    MessageFormat.format("La date de naissance de début ({0}) est après la date de fin ({1}).", fromBirthDate, toBirthDate)
            );
        }
        if (Objects.nonNull(fromDeathDate) && Objects.nonNull(toDeathDate) && fromDeathDate.isAfter(toDeathDate)) {
            throw new InvalidDateException(
                    "fromDeathDate - toDeathDate",
                    MessageFormat.format("La date de décès de début ({0}) est après la date de fin ({1}).", fromDeathDate, toDeathDate)
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
