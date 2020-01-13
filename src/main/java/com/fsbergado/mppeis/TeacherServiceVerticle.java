package com.fsbergado.mppeis;

import com.fsbergado.mppeis.database.DBManager;
import com.fsbergado.mppeis.database.TeacherMapper;
import com.fsbergado.mppeis.models.Teacher;
import com.fsbergado.mppeis.utils.MessageDispatcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * TeacherService
 */
public class TeacherServiceVerticle extends AbstractVerticle {

    public static final String VERTX_EVENT_BUS_SERVICE_ADDRESS = "teacher.service.queue";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        final DBManager db = new DBManager(vertx);
        final MessageDispatcher messageDispatcher = new MessageDispatcher(db, "teachers", new Teacher(), new TeacherMapper());

        db.getConnection(ar -> {
            if (ar.succeeded()) {
                vertx.eventBus().consumer(VERTX_EVENT_BUS_SERVICE_ADDRESS, messageDispatcher);
                System.out.println("Teacher service listening on event bus address: \"" + VERTX_EVENT_BUS_SERVICE_ADDRESS + "\"...");
                Future.succeededFuture();
            } else {
                ar.cause().printStackTrace();
                System.out.println("Failed starting the teacher service.");
                Future.failedFuture(ar.cause());
            }
        });
    }
}
