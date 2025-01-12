package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Director;
import org.desha.app.domain.entity.Person;

import java.util.Set;

@Getter
@Builder
public class TechnicalSummaryDTO {

    private Set<Person> producers;
    private Set<Director> directors;
    private Set<Person> screenwriters;
    private Set<Person> musicians;
    private Set<Person> photographers;
    private Set<Person> costumiers;
    private Set<Person> decorators;
    private Set<Person> editors;
    private Set<Person> casters;

    public static TechnicalSummaryDTO build(
            final Set<Person> producers,
            final Set<Director> directors,
            final Set<Person> screenwriters,
            final Set<Person> musicians,
            final Set<Person> photographers,
            final Set<Person> costumiers,
            final Set<Person> decorators,
            final Set<Person> editors,
            final Set<Person> casting
    ) {
        return TechnicalSummaryDTO.builder()
                .producers(producers)
                .directors(directors)
                .screenwriters(screenwriters)
                .musicians(musicians)
                .photographers(photographers)
                .costumiers(costumiers)
                .decorators(decorators)
                .editors(editors)
                .casters(casting)
                .build();
    }
}
