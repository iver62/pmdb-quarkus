package org.desha.app.domain.record;

import org.desha.app.domain.entity.Movie;

public record MovieWithAwardsNumber(Movie movie, Long awardsNumber) {
}
