package com.fsbergado.mppeis;

import com.fsbergado.mppeis.database.DBManager;
import com.fsbergado.mppeis.database.SchoolMapper;
import com.fsbergado.mppeis.models.School;
import com.fsbergado.mppeis.utils.MessageDispatcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * SchoolService
 */
public class SchoolServiceVerticle extends AbstractVerticle {

    public static final String VERTX_EVENT_BUS_SERVICE_ADDRESS = "school.service.queue";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        final DBManager db = new DBManager(vertx);
        final MessageDispatcher messageDispatcher = new MessageDispatcher(db, "schools", new School(), new SchoolMapper());

        db.getConnection(ar -> {
            if (ar.succeeded()) {
                vertx.eventBus().consumer(VERTX_EVENT_BUS_SERVICE_ADDRESS, messageDispatcher);
                System.out.println("School service listening on event bus address: \""
                        + VERTX_EVENT_BUS_SERVICE_ADDRESS + "\"...");
                Future.succeededFuture();
            } else {
                ar.cause().printStackTrace();
                System.out.println("Failed starting the school service.");
                Future.failedFuture(ar.cause());
            }
        });
    }
}
