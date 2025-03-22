package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RepartitionDTO {

    private String label;
    private Long total;

}
