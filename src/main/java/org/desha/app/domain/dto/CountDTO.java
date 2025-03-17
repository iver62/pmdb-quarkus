package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CountDTO {

    private String name;
    private Long total;

}
