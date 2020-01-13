package com.fsbergado.mppeis;

import com.fsbergado.mppeis.database.DBManager;
import com.fsbergado.mppeis.database.UserMapper;
import com.fsbergado.mppeis.models.User;
import com.fsbergado.mppeis.utils.MessageDispatcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * UserService
 */
public class UserServiceVerticle extends AbstractVerticle {

    public static final String VERTX_EVENT_BUS_SERVICE_ADDRESS = "user.service.queue";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        final DBManager db = new DBManager(vertx);
        final MessageDispatcher messageDispatcher = new MessageDispatcher(db, "users", new User(), new UserMapper());

        db.getConnection(ar -> {
            if (ar.succeeded()) {
                vertx.eventBus().consumer(VERTX_EVENT_BUS_SERVICE_ADDRESS, messageDispatcher);
                System.out.println("User service listening on event bus address: \"" + VERTX_EVENT_BUS_SERVICE_ADDRESS + "\"...");
                Future.succeededFuture();
            } else {
                ar.cause().printStackTrace();
                System.out.println("Failed starting the user service.");
                Future.failedFuture(ar.cause());
            }
        });
    }
}
