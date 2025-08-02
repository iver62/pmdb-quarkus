package org.desha.app.domain.dto;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.desha.app.exception.InvalidSortException;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Getter
public class QueryParamsDTO {

    @Parameter(
            name = "page",
            description = "Numéro de la page à récupérer",
            in = ParameterIn.QUERY
    )
    @QueryParam("page")
    @DefaultValue("0")
    private int pageIndex;

    @Parameter(
            name = "size",
            description = "Nombre d’éléments par page",
            in = ParameterIn.QUERY
    )
    @QueryParam("size")
    @DefaultValue("50")
    private int size;

    @Parameter(
            name = "sort",
            description = "Champ sur lequel appliquer le tri (ex: title, creationDate)",
            in = ParameterIn.QUERY
    )
    @QueryParam("sort")
    private String sort;

    @Parameter(
            name = "direction",
            description = "Direction du tri : Ascending (croissant) ou Descending (décroissant)",
            in = ParameterIn.QUERY
    )
    @QueryParam("direction")
    @DefaultValue("Ascending")
    private String direction;

    @QueryParam("term")
    private String term;

    @QueryParam("lang")
    private String lang;

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
    public void validateSortField(String sort, Set<String> allowedSortFields) {
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

    /**
     * Valide la langue fournie en paramètre.
     * <p>
     * Si aucune langue n'est précisée (null), retourne "fr" par défaut.
     * Seules les valeurs "fr" (français) et "en" (anglais) sont autorisées.
     * <p>
     * Si une valeur non autorisée est fournie, une WebApplicationException
     * est levée avec un code HTTP 400 (Bad Request).
     *
     * @return La langue validée en minuscules ("fr" ou "en").
     * @throws WebApplicationException si la langue n'est ni "fr" ni "en".
     */
    public String validateLang() {
        if (Objects.isNull(lang)) {
            return "fr";
        }
        if ("fr".equalsIgnoreCase(lang) || "en".equalsIgnoreCase(lang)) {
            return lang.toLowerCase();
        }
        throw new WebApplicationException("Le paramètre lang est invalide. Les valeurs autorisées sont 'fr' or 'en'.", Response.Status.BAD_REQUEST);
    }

}
