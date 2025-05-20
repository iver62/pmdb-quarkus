package org.desha.app.domain.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import org.desha.app.domain.PersonType;
import org.desha.app.exception.InvalidDateException;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public class PersonQueryParamsDTO extends QueryParamsDTO {

    @QueryParam("country")
    private List<Integer> countryIds;

    @QueryParam("type")
    private Set<PersonType> personTypes;

    @QueryParam("from-birth-date")
    private LocalDate fromBirthDate;

    @QueryParam("to-birth-date")
    private LocalDate toBirthDate;

    @QueryParam("from-death-date")
    private LocalDate fromDeathDate;

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
