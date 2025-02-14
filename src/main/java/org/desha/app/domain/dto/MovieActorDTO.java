package org.desha.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieActorDTO {

    private Long id;
    private PersonDTO actor;
    private String role;
    private Integer rank;

}
