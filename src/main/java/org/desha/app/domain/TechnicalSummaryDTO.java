package org.desha.app.domain;

import lombok.Getter;

import java.util.Set;

@Getter
public class TechnicalSummaryDTO {

    private Set<Person> producers;
    private Set<Person> directors;
    private Set<Person> screenwriters;
    private Set<Person> musicians;
    private Set<Person> photographers;
    private Set<Person> costumiers;
    private Set<Person> decorators;
    private Set<Person> editors;
    private Set<Person> casting;
}
