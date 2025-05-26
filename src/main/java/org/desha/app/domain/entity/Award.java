package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.AwardDTO;

import java.time.Year;
import java.util.Set;

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

    @NotBlank(message = "La cérémonie ne peut pas être vide")
    @Column(name = "ceremonie", nullable = false)
    private String ceremony;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "annee")
    private Year year;

    @ManyToOne
    @JoinColumn(name = "fk_film", nullable = false)
    private Movie movie;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "lnk_recompense_personne",
            joinColumns = @JoinColumn(name = "fk_recompense"),
            inverseJoinColumns = @JoinColumn(name = "fk_personne")
    )
    private Set<Person> personSet;

    public static Award fromDTO(AwardDTO awardDTO) {
        return
                Award.builder()
                        .id(awardDTO.getId())
                        .ceremony(StringUtils.capitalize(awardDTO.getCeremony().trim()))
                        .name(StringUtils.capitalize(awardDTO.getName().trim()))
                        .year(awardDTO.getYear())
                        .build();
    }

}
