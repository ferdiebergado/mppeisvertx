package com.fsbergado.mppeis.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * FinalResponseHandler
 */
public class FinalResponseHandler implements Handler<RoutingContext> {

    public static final String ALLOWED_ORIGINS = "*";

    @Override
    public void handle(RoutingContext event) {
        Object payload = event.get("payload");
        event.response().putHeader("Access-Control-Allow-Origin", ALLOWED_ORIGINS).putHeader("Content-Type", "application/json").end(Json.encodeToBuffer(payload));
    }
}