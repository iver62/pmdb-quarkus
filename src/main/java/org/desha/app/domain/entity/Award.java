package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.LitePersonDTO;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Year;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "recompense")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Award extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_ceremonie_recompenses")
    private CeremonyAwards ceremonyAwards;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "annee")
    private Year year;

    @ManyToMany
    @JoinTable(
            name = "lnk_recompense_personne",
            joinColumns = @JoinColumn(name = "fk_recompense"),
            inverseJoinColumns = @JoinColumn(name = "fk_personne")
    )
    @Fetch(FetchMode.JOIN)
    private Set<Person> personSet;

    public static Award build(Long id, String name, Year year) {
        return
                Award.builder()
                        .id(id)
                        .name(StringUtils.capitalize(name).trim())
                        .year(year)
                        .build()
                ;
    }

    public static Award createAward(AwardDTO awardDTO, CeremonyAwards ceremonyAwards, Map<Long, Person> personMap) {
        Award newAward = Award.build(awardDTO.getId(), awardDTO.getName(), awardDTO.getYear());
        newAward.setCeremonyAwards(ceremonyAwards);

        if (Objects.nonNull(awardDTO.getPersons())) {
            Set<Person> linkedPersons = awardDTO.getPersons().stream()
                    .map(LitePersonDTO::getId)
                    .map(personMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            newAward.setPersonSet(linkedPersons);
        }

        return newAward;
    }

    public void updateAward(AwardDTO awardDTO, Map<Long, Person> personMap) {
        setName(StringUtils.capitalize(StringUtils.defaultString(awardDTO.getName()).trim()));
        setYear(awardDTO.getYear());

        if (Objects.nonNull(awardDTO.getPersons())) {
            Set<Person> linkedPersons = awardDTO.getPersons().stream()
                    .map(LitePersonDTO::getId)
                    .map(personMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            setPersonSet(linkedPersons);
        }
    }

}
