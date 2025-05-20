package org.desha.app.domain.record;

import org.desha.app.domain.entity.Person;

public record PersonWithMoviesNumber(Person person, Long number) {
}
