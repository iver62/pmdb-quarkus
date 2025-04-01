package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.repository.AwardRepository;

import java.util.List;

@ApplicationScoped
public class AwardService {

    private final AwardRepository awardRepository;

    @Inject
    public AwardService(AwardRepository awardRepository) {
        this.awardRepository = awardRepository;
    }

    public Uni<List<String>> findCeremonies() {
        return awardRepository.findCeremonies();
    }
}
