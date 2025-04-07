package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.repository.AwardRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Set<AwardDTO> fromAwardSetEntity(Set<Award> awardSet) {
        return
                awardSet
                        .stream()
                        .map(AwardDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }
}
