package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;

import java.time.Year;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwardDTO {

    private Long id;
    private String ceremony;
    private String name;
    private Year year;
    private MovieDTO movie;

    public static AwardDTO fromEntity(Award award) {
        return
                AwardDTO.builder()
                        .id(award.getId())
                        .ceremony(award.getCeremony())
                        .name(award.getName())
                        .year(award.getYear())
                        .movie(MovieDTO.fromEntity(award.getMovie()))
                        .build();
    }
}
