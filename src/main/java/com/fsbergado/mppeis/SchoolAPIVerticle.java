package com.fsbergado.mppeis;

import java.util.HashSet;
import java.util.Set;

import com.fsbergado.mppeis.handlers.FinalResponseHandler;
import com.fsbergado.mppeis.handlers.school.SchoolBodyHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;

/**
 * SchoolAPIVerticle
 */
public class SchoolAPIVerticle extends AbstractVerticle {

    public static final String VERTX_EVENT_BUS_DB_ADDRESS = "db.queue";
    public static final String VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE = "school.service.queue";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        final Router router = Router.router(vertx);        
        final int API_PORT = Integer.parseInt(System.getenv("API_PORT"));
        final String API_URL = "/api/v1/schools";
        final String API_URL_ID = API_URL + "/:id";

        SchoolBodyHandler schoolBodyHandler = new SchoolBodyHandler();
        FinalResponseHandler finalResponseHandler = new FinalResponseHandler();

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        // allowedHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        /*
         * these methods aren't necessary for this sample, but you may need them for
         * your projects
         */
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        router.route().consumes("application/json").handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods)).handler(BodyHandler.create()).handler(LoggerHandler.create()).failureHandler(ctx -> {
            Throwable failure = ctx.failure();
            failure.printStackTrace();
            ctx.response().putHeader("Access-Control-Allow-Origin", FinalResponseHandler.ALLOWED_ORIGINS).putHeader("Content-Type", "application/json").setStatusCode(500).end(Json.encodeToBuffer(new JsonObject().put("error", failure.getMessage())));
        });

        // API ROUTES //

        // CREATE A SCHOOL
        router.post(API_URL).handler(schoolBodyHandler).handler(this::addSchoolHandler).handler(finalResponseHandler);

        // SHOW ALL SCHOOLS
        router.get(API_URL).handler(this::showAllSchoolsHandler).handler(finalResponseHandler);

        // GET A SCHOOL BY ID
        router.get(API_URL_ID).handler(this::getSchoolHandler).handler(finalResponseHandler);;

        // UPDATE A SCHOOL
        router.put(API_URL_ID).handler(this::updateSchoolHandler).handler(finalResponseHandler);;

        // DELETE A SCHOOL
        router.delete(API_URL_ID).handler(this::deleteSchoolHandler).handler(finalResponseHandler);

        // Launch the http server
        vertx.createHttpServer(new HttpServerOptions().setLogActivity(true)).requestHandler(router).listen(API_PORT, http -> {
            if (http.succeeded()) {
                System.out.println("HTTP Server listening on port " + API_PORT + "...");
                System.out.println("School API endpoint available at " + API_URL + ".");
                Future.succeededFuture();
            } else {
                http.cause().printStackTrace();
                System.out.println("Can't start the School API Server.");
                Future.failedFuture(http.cause());
            }
        });
    }

    private void showAllSchoolsHandler(RoutingContext ctx) {

        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "index");

        vertx.eventBus().request(VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE, new JsonObject(), deliveryOptions, reply -> {
            if (reply.failed()) {
                System.out.println("Failed to fetch school list.");
                ctx.fail(reply.cause());
                return;
            }
            ctx.put("payload", reply.result().body());
            ctx.next();
        });
    }

    private void getSchoolHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "show");

        final int id = Integer.parseInt(ctx.request().getParam("id"));

        vertx.eventBus().request(VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE,
                new JsonObject().put("id", id), deliveryOptions,
                reply -> {
                    if (reply.failed()) {
                        ctx.fail(reply.cause());
                        return;
                    }
                    
                    ctx.put("payload", reply.result().body());
                    ctx.next();                    
                });
    }

    private void addSchoolHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "create");
        final JsonObject body = ctx.getBodyAsJson();

        vertx.eventBus().request(VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE, new JsonObject().put("params", body),
                deliveryOptions, reply -> {
                    if (reply.failed()) {
                        ctx.fail(reply.cause());
                        return;
                    }

                    ctx.put("payload", reply.result().body());
                    ctx.response().setStatusCode(201);
                    ctx.next();
                });
    }

    private void updateSchoolHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "update");
        final JsonObject body = ctx.getBodyAsJson();
        final int id = Integer.parseInt(ctx.request().getParam("id"));

        vertx.eventBus().request(VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE, new JsonObject().put("id", id).put("params", body),
                deliveryOptions, reply -> {
                    if (reply.failed()) {
                        ctx.fail(reply.cause());
                        return;
                    }

                    ctx.put("payload", reply.result().body());
                    ctx.next();
                });
    }

    private void deleteSchoolHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "destroy");

        final int id = Integer.parseInt(ctx.request().getParam("id"));

        vertx.eventBus().request(VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE, new JsonObject().put("id", id),
                deliveryOptions, reply -> {
                    if (reply.failed()) {
                        ctx.fail(reply.cause());
                        return;
                    }

                    ctx.response().setStatusCode(204);
                    ctx.next();
                });
    }
}