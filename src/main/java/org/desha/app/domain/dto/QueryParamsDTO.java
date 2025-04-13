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

    /**
     * Valide que le champ de tri spécifié fait partie des champs autorisés.
     *
     * @param sort              Le nom du champ de tri à valider.
     * @param allowedSortFields La liste des champs de tri autorisés.
     * @throws InvalidSortException si le champ de tri n'est pas autorisé.
     */
    public void validateSortField(String sort, List<String> allowedSortFields) {
        if (!allowedSortFields.contains(sort)) {
            throw new InvalidSortException(MessageFormat.format("Le champ de tri \"{0}\" est invalide. Valeurs autorisées : {1}", sort, allowedSortFields));
        }
    }

    /**
     * Valide et retourne la direction de tri spécifiée par l'utilisateur.
     * <p>
     * Si la valeur de {@code direction} correspond (en ignorant la casse) à l'une des valeurs
     * de l'énumération {@link Sort.Direction}, celle-ci est retournée.
     * Sinon, la direction {@code Sort.Direction.Ascending} est utilisée par défaut.
     *
     * @return La direction de tri validée, ou {@code Sort.Direction.Ascending} si invalide.
     */
    public Sort.Direction validateSortDirection() {
        return Arrays.stream(Sort.Direction.values())
                .filter(d -> d.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(Sort.Direction.Ascending); // Valeur par défaut si invalide
    }

}
