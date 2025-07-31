package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CeremonyDTO {

    private Long id;
    @NotNull(message = "Le nom de la cérémonie est obligatoire")
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
