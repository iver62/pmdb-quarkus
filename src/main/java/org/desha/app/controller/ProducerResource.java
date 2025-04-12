package org.desha.app.controller;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.desha.app.domain.entity.Producer;
import org.desha.app.service.ProducerService;

@Path("producers")
@Singleton
public class ProducerResource extends PersonResource<Producer> {

    @Inject
    public ProducerResource(ProducerService producerService) {
        super(producerService);
    }

}
