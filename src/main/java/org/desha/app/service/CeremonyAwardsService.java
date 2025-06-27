package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.desha.app.domain.dto.LightPersonDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.CeremonyAwards;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class CeremonyAwardsService {

    private final CeremonyService ceremonyService;

    @Inject
    public CeremonyAwardsService(CeremonyService ceremonyService) {
        this.ceremonyService = ceremonyService;
    }

    public Uni<CeremonyAwards> createNewCeremonyAwards(Movie movie, CeremonyAwardsDTO ceremonyAwardsDTO, Map<Long, Person> personMap) {
        return
                ceremonyService.findOrCreateCeremony(ceremonyAwardsDTO.getCeremony())
                        .map(ceremony -> CeremonyAwards.build(ceremony, movie, new ArrayList<>()))
                        .invoke(ceremonyAwards -> {
                                    if (Objects.nonNull(ceremonyAwardsDTO.getAwards())) {
                                        for (AwardDTO awardDTO : ceremonyAwardsDTO.getAwards()) {
                                            Award award = Award.of(awardDTO);
                                            award.setCeremonyAwards(ceremonyAwards);

                                            if (Objects.nonNull(awardDTO.getPersons())) {
                                                Set<Person> linkedPersons = awardDTO.getPersons().stream()
                                                        .map(LightPersonDTO::getId)
                                                        .map(personMap::get)
                                                        .filter(Objects::nonNull)
                                                        .collect(Collectors.toSet());

                                                award.setPersonSet(linkedPersons);
                                            }

                                            ceremonyAwards.getAwards().add(award);
                                        }
                                    }
                                }
                        )
                ;
    }

}
