package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.desha.app.domain.entity.Actor;
import org.desha.app.domain.entity.MovieActor;
import org.desha.app.repository.ActorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Set;

@Dependent
public class ActorService extends PersonService<Actor> {

    @Inject
    public ActorService(CountryService countryService, ActorRepository actorRepository, FileService fileService) {
        super(countryService, actorRepository, fileService);
    }

    public Uni<Set<MovieActor>> getMovieActors(Actor actor) {
        return Mutiny.fetch(actor.getMovieActors());
    }

}
