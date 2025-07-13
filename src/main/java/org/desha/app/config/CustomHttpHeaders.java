package org.desha.app.config;

import io.vertx.mutiny.core.http.HttpHeaders;

public class CustomHttpHeaders extends HttpHeaders {

    public static final String X_TOTAL_COUNT = "X-Total-Count";

    public CustomHttpHeaders(io.vertx.core.http.HttpHeaders delegate) {
        super(delegate);
    }
}
