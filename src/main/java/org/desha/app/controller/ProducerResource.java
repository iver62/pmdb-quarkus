package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Producer;
import org.desha.app.service.ProducerService;

@Path("producers")
@Singleton
@Slf4j
public class ProducerResource extends PersonResource<Producer> {

    @Inject
    public ProducerResource(ProducerService producerService) {
        super(producerService);
    }

}
