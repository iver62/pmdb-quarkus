package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Ceremony;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CeremonyDTO {

    private Long id;
    private String name;

    public static CeremonyDTO build(Long id, String name) {
        return
                CeremonyDTO.builder()
                        .id(id)
                        .name(name)
                        .build()
                ;
    }

    public static CeremonyDTO of(Ceremony ceremony) {
        return CeremonyDTO.build(ceremony.getId(), ceremony.getName());
    }

    public static Set<CeremonyDTO> fromEntityList(List<Ceremony> ceremonyList) {
        return
                ceremonyList.stream()
                        .map(CeremonyDTO::of)
                        .collect(Collectors.toSet())
                ;
    }
}
