package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {

    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public static CategoryDTO build(Long id, String name, LocalDateTime creationDate, LocalDateTime lastUpdate) {
        return
                CategoryDTO.builder()
                        .id(id)
                        .name(name)
                        .creationDate(creationDate)
                        .lastUpdate(lastUpdate)
                        .build()
                ;
    }

}
