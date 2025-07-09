package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.Repartition;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieStats {

    private final long movieCount;
    private final long actorCount;
    private final List<Repartition> byReleaseDate;
    private final List<Repartition> byCategory;
    private final List<CountryRepartition> byCountry;
    private final List<Repartition> byUser;
    private final List<Repartition> byCreationDate;
    private final List<Repartition> moviesNumberEvolution;
    private final List<Repartition> actorsNumberEvolution;

    public static MovieStats build(long movieCount, long actorCount, List<Repartition> byReleaseDate, List<Repartition> byCategory, List<CountryRepartition> byCountry, List<Repartition> byUser, List<Repartition> byCreationDate, List<Repartition> moviesNumberEvolution, List<Repartition> actorsNumberEvolution) {
        return
                MovieStats.builder()
                        .movieCount(movieCount)
                        .actorCount(actorCount)
                        .byReleaseDate(byReleaseDate)
                        .byCategory(byCategory)
                        .byCountry(byCountry)
                        .byUser(byUser)
                        .byCreationDate(byCreationDate)
                        .moviesNumberEvolution(moviesNumberEvolution)
                        .actorsNumberEvolution(actorsNumberEvolution)
                        .build();
    }
}
