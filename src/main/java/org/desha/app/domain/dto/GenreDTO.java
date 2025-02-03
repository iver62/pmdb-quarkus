package org.desha.app.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GenreDTO {

    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

}
