package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiteMovieDTO {

    private Long id;
    private String title;
    private String originalTitle;
    private LocalDate releaseDate;

}
