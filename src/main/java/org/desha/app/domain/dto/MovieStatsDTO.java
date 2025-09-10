package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.Repartition;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Statistiques sur les films et acteurs")
public class MovieStatsDTO {

    @Schema(description = "Nombre total de films", type = SchemaType.NUMBER, examples = "120")
    private final long movieCount;

    @Schema(description = "Nombre total d'acteurs", type = SchemaType.NUMBER, examples = "450")
    private final long actorCount;

    @Schema(description = "Répartition des films par date de sortie", type = SchemaType.ARRAY)
    private final List<Repartition> byReleaseDate;

    @Schema(description = "Répartition des films par catégorie", type = SchemaType.ARRAY)
    private final List<Repartition> byCategory;

    @Schema(description = "Répartition des films par pays", type = SchemaType.ARRAY)
    private final List<CountryRepartition> byCountry;

    @Schema(description = "Répartition des films par utilisateur ayant soumis", type = SchemaType.ARRAY)
    private final List<Repartition> byUser;

    @Schema(description = "Répartition des films par date de création de l'enregistrement", type = SchemaType.ARRAY)
    private final List<Repartition> byCreationDate;

    @Schema(description = "Évolution du nombre de films au fil du temps", type = SchemaType.ARRAY)
    private final List<Repartition> moviesNumberEvolution;

    @Schema(description = "Évolution du nombre d'acteurs au fil du temps", type = SchemaType.ARRAY)
    private final List<Repartition> actorsNumberEvolution;

    public static MovieStatsDTO build(long movieCount, long actorCount, List<Repartition> byReleaseDate, List<Repartition> byCategory, List<CountryRepartition> byCountry, List<Repartition> byUser, List<Repartition> byCreationDate, List<Repartition> moviesNumberEvolution, List<Repartition> actorsNumberEvolution) {
        return
                MovieStatsDTO.builder()
                        .movieCount(movieCount)
                        .actorCount(actorCount)
                        .byReleaseDate(byReleaseDate)
                        .byCategory(byCategory)
                        .byCountry(byCountry)
                        .byUser(byUser)
                        .byCreationDate(byCreationDate)
                        .moviesNumberEvolution(moviesNumberEvolution)
                        .actorsNumberEvolution(actorsNumberEvolution)
                        .build()
                ;
    }
}
