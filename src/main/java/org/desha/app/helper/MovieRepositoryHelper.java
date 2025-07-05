package org.desha.app.helper;

import io.quarkus.panache.common.Sort;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.PersonType;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class MovieRepositoryHelper extends SqlHelper {

    private final Map<PersonType, String> ROLE_ENTITIES = Map.ofEntries(
            Map.entry(PersonType.ACTOR, "MovieActor ma"),
            Map.entry(PersonType.DIRECTOR, "MovieDirector md"),
            Map.entry(PersonType.ASSISTANT_DIRECTOR, "MovieAssistantDirector mad"),
            Map.entry(PersonType.SCREENWRITER, "MovieScreenwriter msc"),
            Map.entry(PersonType.PRODUCER, "MovieProducer mpr"),
            Map.entry(PersonType.COMPOSER, "MovieComposer mc"),
            Map.entry(PersonType.MUSICIAN, "MovieMusician mm"),
            Map.entry(PersonType.PHOTOGRAPHER, "MoviePhotographer mph"),
            Map.entry(PersonType.COSTUME_DESIGNER, "MovieCostumeDesigner mcd"),
            Map.entry(PersonType.SET_DESIGNER, "MovieSetDesigner msd"),
            Map.entry(PersonType.EDITOR, "MovieEditor me"),
            Map.entry(PersonType.CASTER, "MovieCaster mc"),
            Map.entry(PersonType.ARTIST, "MovieArtist ma"),
            Map.entry(PersonType.SOUND_EDITOR, "MovieSoundEditor mse"),
            Map.entry(PersonType.VFX_SUPERVISOR, "MovieVfxSupervisor mvs"),
            Map.entry(PersonType.SFX_SUPERVISOR, "MovieSfxSupervisor mss"),
            Map.entry(PersonType.MAKEUP_ARTIST, "MovieMakeupArtist mma"),
            Map.entry(PersonType.HAIR_DRESSER, "MovieHairDresser mhd"),
            Map.entry(PersonType.STUNT_MAN, "MovieStuntman mst")
    );

    public String buildExistsClause(Person person) {
        return person.getTypes().stream()
                .map(personType -> "EXISTS (SELECT 1 FROM " + ROLE_ENTITIES.get(personType) + " WHERE " + ROLE_ENTITIES.get(personType).split(" ")[1] + ".movie = m AND " + ROLE_ENTITIES.get(personType).split(" ")[1] + ".person = :person)")
                .collect(Collectors.joining(" OR "));
    }

    public String addSort(String sort, Sort.Direction direction) {
        if (StringUtils.isEmpty(sort)) return "";

        String dir = (direction == Sort.Direction.Ascending) ? "ASC" : "DESC";

        // Si le critère de tri est le nombre de récompenses
        if ("awardsCount".equals(sort)) {
            return String.format(" ORDER BY SIZE(m.awards) %s", dir);
        }

        // Protection basique contre injection ou champ non mappé
        Set<String> allowedFields = Movie.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN m.%s IS NULL THEN 1 ELSE 0 END, m.%s %s", sort, sort, dir);
    }

    public String addClauses(CriteriasDTO criteriasDTO) {
        StringBuilder query = new StringBuilder();

        Optional.ofNullable(criteriasDTO.getFromReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate >= :fromReleaseDate"));
        Optional.ofNullable(criteriasDTO.getToReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate <= :toReleaseDate"));
        Optional.ofNullable(criteriasDTO.getFromCreationDate()).ifPresent(date -> query.append(" AND m.creationDate >= :fromCreationDate"));
        Optional.ofNullable(criteriasDTO.getToCreationDate()).ifPresent(date -> query.append(" AND m.creationDate <= :toCreationDate"));
        Optional.ofNullable(criteriasDTO.getFromLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate >= :fromLastUpdate"));
        Optional.ofNullable(criteriasDTO.getToLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate <= :toLastUpdate"));

        if (Objects.nonNull(criteriasDTO.getCategoryIds()) && !criteriasDTO.getCategoryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.categories c WHERE c.id IN :categoryIds)");
        }

        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.countries c WHERE c.id IN :countryIds)");
        }

        if (Objects.nonNull(criteriasDTO.getUserIds()) && !criteriasDTO.getUserIds().isEmpty()) {
            query.append(" AND m.user.id IN :userIds");
        }

        return query.toString();
    }
}
