package com.fsbergado.mppeis.handlers.school;

import com.fsbergado.mppeis.models.School;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * SchoolBodyHandler
 */
public class SchoolBodyHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext event) {
        final JsonObject body = event.getBodyAsJson();

        if (body.isEmpty()) {
            event.response().setStatusCode(400).end("Request body missing.");
            return;
        }

        final School school = body.mapTo(School.class);
        event.put("school", school);
        event.next();
    }
}