package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;

import java.time.Year;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwardDTO {

    private Long id;
    private String ceremony;
    private String name;
    private MovieDTO movie;
    private Set<PersonDTO> persons;
    private Year year;

    public static AwardDTO fromEntity(Award award) {
        return
                AwardDTO.builder()
                        .id(award.getId())
                        .ceremony(award.getCeremony())
                        .name(award.getName())
                        .movie(MovieDTO.fromEntity(award.getMovie()))
                        .persons(PersonDTO.fromEntitySet(award.getPersonSet()))
                        .year(award.getYear())
                        .build();
    }

    public static Set<AwardDTO> fromEntitySet(Set<Award> awardSet) {
        return
                Optional.ofNullable(awardSet).orElse(Set.of())
                        .stream()
                        .map(AwardDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }
}
