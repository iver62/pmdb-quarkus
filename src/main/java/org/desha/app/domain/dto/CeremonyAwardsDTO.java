package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.CeremonyAwards;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CeremonyAwardsDTO {

    private Long id;
    private CeremonyDTO ceremony;
    private LightMovieDTO movie;
    private List<AwardDTO> awards;

    public static CeremonyAwardsDTO build(Long id, CeremonyDTO ceremony, LightMovieDTO movie, List<AwardDTO> awards) {
        return
                CeremonyAwardsDTO.builder()
                        .id(id)
                        .ceremony(ceremony)
                        .movie(movie)
                        .awards(awards)
                        .build();
    }

    public static CeremonyAwardsDTO of(CeremonyAwards ceremonyAwards) {
        return
                CeremonyAwardsDTO.build(
                        ceremonyAwards.getId(),
                        CeremonyDTO.of(ceremonyAwards.getCeremony()),
                        LightMovieDTO.of(ceremonyAwards.getMovie()),
                        null
                );
    }

    public static CeremonyAwardsDTO of(CeremonyAwards ceremonyAwards, List<Award> awardList) {
        return
                CeremonyAwardsDTO.build(
                        ceremonyAwards.getId(),
                        CeremonyDTO.of(ceremonyAwards.getCeremony()),
                        LightMovieDTO.of(ceremonyAwards.getMovie()),
                        AwardDTO.fromEntityListWithPersons(awardList)
                );
    }

    public static Set<CeremonyAwardsDTO> fromEntitySet(Set<CeremonyAwards> ceremonyAwardsSet) {
        return
                Optional.ofNullable(ceremonyAwardsSet).orElse(Set.of())
                        .stream()
                        .map(ceremonyAwards -> CeremonyAwardsDTO.of(ceremonyAwards, ceremonyAwards.getAwards()))
                        .collect(Collectors.toSet())
                ;
    }

    public static Set<CeremonyAwardsDTO> fromEntityList(List<CeremonyAwards> ceremonyAwardsList) {
        return
                Optional.ofNullable(ceremonyAwardsList).orElse(List.of())
                        .stream()
                        .map(ceremonyAwards -> CeremonyAwardsDTO.of(ceremonyAwards, ceremonyAwards.getAwards()))
                        .collect(Collectors.toSet())
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
                ", awards=" + (Objects.nonNull(awards) ? awards.stream().map(AwardDTO::getId).toList() : "[]") +
                '}';
    }

}
