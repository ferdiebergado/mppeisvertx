package com.fsbergado.mppeis.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * FailureHandler
 */
public class FailureHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        failure.printStackTrace();
        ctx.response().putHeader("Content-Type", "application/json").setStatusCode(500)
                .end(Json.encodeToBuffer(new JsonObject().put("error", failure.getMessage())));
    }
}