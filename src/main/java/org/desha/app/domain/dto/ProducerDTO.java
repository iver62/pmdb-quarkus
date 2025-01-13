package org.desha.app.domain.dto;

import lombok.Getter;
import org.desha.app.domain.entity.Movie;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ProducerDTO extends PersonDTO {

    private final Set<Movie> movies = new HashSet<>();

}
