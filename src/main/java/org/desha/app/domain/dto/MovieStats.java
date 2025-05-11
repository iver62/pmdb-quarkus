package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.Repartition;

import java.util.List;

@Getter
@Builder
public class MovieStats {

    private final long movieCount;
    private final long actorCount;
    private final List<Repartition> byReleaseDate;
    private final List<Repartition> byGenre;
    private final List<CountryRepartition> byCountry;
    private final List<Repartition> byUser;
    private final List<Repartition> byCreationDate;
    private final List<Repartition> numberEvolution;

    public static MovieStats build(long movieCount, long actorCount, List<Repartition> byReleaseDate, List<Repartition> byGenre, List<CountryRepartition> byCountry, List<Repartition> byUser, List<Repartition> byCreationDate, List<Repartition> numberEvolution) {
        return
                MovieStats.builder()
                        .movieCount(movieCount)
                        .actorCount(actorCount)
                        .byReleaseDate(byReleaseDate)
                        .byGenre(byGenre)
                        .byCountry(byCountry)
                        .byUser(byUser)
                        .byCreationDate(byCreationDate)
                        .numberEvolution(numberEvolution)
                        .build();
    }
}
