package com.fsbergado.mppeis.utils;

import com.fsbergado.mppeis.handlers.FinalResponseHandler;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * RouterUtil
 */
public class RouterUtil {

    // private final String API_URL = "/";
    private final String VERTX_EVENT_BUS_SERVICE_ADDRESS;
    private final Vertx vertx;
    private final String API_URL;

    public RouterUtil(Vertx vertx, String url, String address) {
        this.vertx = vertx;
        this.API_URL = url;
        this.VERTX_EVENT_BUS_SERVICE_ADDRESS = address;
    }

    public Router dispatch(Router router) {
        
        final String API_URL_ID = API_URL + "/:id";
        // final Router router = Router.router(vertx);
        // final CorsHandler corsHandler = CorsUtil.handler();

        // router.route().consumes("application/json");
        // router.route().handler(BodyHandler.create());
        // router.route().handler(LoggerHandler.create());
        // router.route().handler(corsHandler);
        // router.route().failureHandler(new FailureHandler());        

        final FinalResponseHandler finalResponseHandler = new FinalResponseHandler();

        // API ROUTES //

        // CREATE A RESOURCE
        router.post(API_URL).handler(this::storeHandler).handler(finalResponseHandler);

        // SHOW ALL RESOURCES
        router.get(API_URL).handler(this::indexHandler).handler(finalResponseHandler);

        // GET A RESOURCE BY ID
        router.get(API_URL_ID).handler(this::showHandler).handler(finalResponseHandler);

        // UPDATE A RESOURCE
        router.put(API_URL_ID).handler(this::updateHandler).handler(finalResponseHandler);

        // DELETE A RESOURCE
        router.delete(API_URL_ID).handler(this::destroyHandler).handler(finalResponseHandler);

        return router;
    }

    private void storeHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "create");
        final JsonObject body = ctx.getBodyAsJson();

        vertx.eventBus().request(VERTX_EVENT_BUS_SERVICE_ADDRESS, new JsonObject().put("params", body),
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

    private void indexHandler(RoutingContext ctx) {

        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "index");

        vertx.eventBus().request(VERTX_EVENT_BUS_SERVICE_ADDRESS, new JsonObject(), deliveryOptions,
                reply -> {
                    if (reply.failed()) {
                        System.out.println("Failed to fetch school list.");
                        ctx.fail(reply.cause());
                        return;
                    }
                    ctx.put("payload", reply.result().body());
                    ctx.next();
                });
    }    

    private void showHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "show");

        final int id = Integer.parseInt(ctx.request().getParam("id"));

        vertx.eventBus().request(VERTX_EVENT_BUS_SERVICE_ADDRESS, new JsonObject().put("id", id),
                deliveryOptions, reply -> {
                    if (reply.failed()) {
                        ctx.fail(reply.cause());
                        return;
                    }

                    ctx.put("payload", reply.result().body());
                    ctx.next();
                });
    }    

    private void updateHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "update");
        final JsonObject body = ctx.getBodyAsJson();
        final int id = Integer.parseInt(ctx.request().getParam("id"));

        vertx.eventBus().request(VERTX_EVENT_BUS_SERVICE_ADDRESS,
                new JsonObject().put("id", id).put("params", body), deliveryOptions, reply -> {
                    if (reply.failed()) {
                        ctx.fail(reply.cause());
                        return;
                    }

                    ctx.put("payload", reply.result().body());
                    ctx.next();
                });
    }

    private void destroyHandler(RoutingContext ctx) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "destroy");

        final int id = Integer.parseInt(ctx.request().getParam("id"));

        vertx.eventBus().request(VERTX_EVENT_BUS_SERVICE_ADDRESS, new JsonObject().put("id", id),
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