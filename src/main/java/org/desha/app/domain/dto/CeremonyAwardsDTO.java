package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CeremonyAwardsDTO {

    private Long id;
    private CeremonyDTO ceremony;
    private LiteMovieDTO movie;
    private List<AwardDTO> awards;

    public static CeremonyAwardsDTO build(Long id, CeremonyDTO ceremony, List<AwardDTO> awards) {
        return
                CeremonyAwardsDTO.builder()
                        .id(id)
                        .ceremony(ceremony)
                        .awards(awards)
                        .build()
                ;
    }

    public static CeremonyAwardsDTO build(Long id, CeremonyDTO ceremony, LiteMovieDTO movie, List<AwardDTO> awards) {
        return
                CeremonyAwardsDTO.builder()
                        .id(id)
                        .ceremony(ceremony)
                        .movie(movie)
                        .awards(awards)
                        .build()
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CeremonyAwardsDTO that = (CeremonyAwardsDTO) o;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CeremonyAwardsDTO{" +
                "id=" + id +
                ", ceremony=" + (Objects.nonNull(ceremony) ? ceremony.getName() : "null") +
                ", movie=" + (Objects.nonNull(movie) ? movie.getTitle() : "null") +
                ", awards=" + (Objects.nonNull(awards) ? awards.stream().map(AwardDTO::getId).toList() : "[]") +
                '}';
    }

}
