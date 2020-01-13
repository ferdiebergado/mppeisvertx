package com.fsbergado.mppeis;

import com.fsbergado.mppeis.handlers.FailureHandler;
import com.fsbergado.mppeis.utils.CorsUtil;
import com.fsbergado.mppeis.utils.RouterUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;

/**
 * HttpServerVerticle
 */
public class HttpServerVerticle extends AbstractVerticle {
    
    public static final String VERTX_EVENT_BUS_SCHOOL_SERVICE_ADDRESS = "school.service.queue";
    public static final String VERTX_EVENT_BUS_TEACHER_SERVICE_ADDRESS = "teacher.service.queue";

    @Override
    public void start(final Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        final int API_PORT = Integer.parseInt(System.getenv("API_PORT"));
        // final String API_URL = "/api/v1/";
        final Router router = Router.router(vertx);
        final CorsHandler corsHandler = CorsUtil.handler();

        router.route().consumes("application/json");
        router.route().handler(BodyHandler.create());
        router.route().handler(LoggerHandler.create());
        router.route().handler(corsHandler);
        router.route().failureHandler(new FailureHandler());

        final Router schoolAPIrouter = new RouterUtil(vertx, "/schools", VERTX_EVENT_BUS_SCHOOL_SERVICE_ADDRESS).dispatch(router);
        final Router teacherAPIrouter = new RouterUtil(vertx, "/teachers", VERTX_EVENT_BUS_TEACHER_SERVICE_ADDRESS).dispatch(router);

        router.mountSubRouter("/schoolsAPI", schoolAPIrouter);
        router.mountSubRouter("/teachersAPI", teacherAPIrouter);

        // Launch the http server
        vertx.createHttpServer(new HttpServerOptions().setLogActivity(true)).requestHandler(router).listen(API_PORT,
                http -> {
                    if (http.succeeded()) {
                        System.out.println("HTTP Server listening on port " + API_PORT + "...");
                        Future.succeededFuture();
                    } else {
                        http.cause().printStackTrace();
                        System.out.println("Can't start HTTP Server.");
                        Future.failedFuture(http.cause());
                    }
                });
    }
}
