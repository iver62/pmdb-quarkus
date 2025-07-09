package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

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
}
