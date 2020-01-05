package com.fsbergado.mppeis;

import java.util.ArrayList;
import java.util.List;

import com.fsbergado.mppeis.exceptions.RecordNotFoundException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

/**
 * DatabaseVerticle
 */
public class DatabaseVerticle extends AbstractVerticle {

    public static final String VERTX_BUS_ADDRESS_DB_QUEUE = "db.queue";

    private PgPool pool;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        final PgConnectOptions connectOptions = new PgConnectOptions().setPort(Integer.parseInt(System.getenv("PGPORT"))).setHost(System.getenv("PGHOST"))
                .setDatabase(System.getenv("PGDATABASE")).setUser(System.getenv("PGUSER")).setPassword(System.getenv("PGPASSWORD")).setCachePreparedStatements(true);

        // Pool options
        final PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        // Create the pooled client
        pool = PgPool.pool(vertx, connectOptions, poolOptions);

        getConnection(ar-> {
            if (ar.succeeded()) {
                vertx.eventBus().consumer(VERTX_BUS_ADDRESS_DB_QUEUE, this::onMessage);
                System.out.println("Connected to the database.");
                Future.succeededFuture();
            } else {
                ar.cause().printStackTrace();
                System.out.println("Failed connecting to the database.");
                Future.failedFuture(ar.cause());
            }
        });
    }

    public void onMessage(Message<JsonObject> message) {
        System.out.println("On message.");

        if (!message.headers().contains("action")) {
            message.fail(404, "No action header specified");
            return;
        }

        final JsonObject body = message.body();
        final String sql = body.getString("sql");

        if (sql.isEmpty()) {
            message.fail(404, "No query statement specified");            
            return;
        }

        final String action = message.headers().get("action");

        switch (action) {
        case "query":
            query(sql, ar -> {
                if (ar.failed()) {
                    message.fail(500, ar.cause().getMessage());
                    return;
                }
                message.reply(new JsonObject().put("data", ar.result()));
            });
            break;
            case "queryMany":
            queryMany(sql, ar -> {
                if (ar.failed()) {
                    message.fail(500, ar.cause().getMessage());
                    return;
                }
                message.reply(new JsonObject().put("data", ar.result()));
            });
            break;
        case "preparedQuery":
            System.out.println("Case.");
            final JsonObject params = body.getJsonObject("params");

            if (params.isEmpty()) {
                message.fail(404, "Query parameters missing.");
                return;
            }

            preparedQuery(sql, params, ar -> {
                System.out.println("Prepared query switch.");
                if (ar.failed()) {
                    message.fail(500, ar.cause().getMessage());
                    return;
                }
                System.out.println("prepared query success.");
                System.out.println(" prepared query result: " + ar.result());
                message.reply(ar.result());
            });
            break;
        case "preparedQueryMany":
            System.out.println("Case.");
            final JsonObject params2 = body.getJsonObject("params");

            if (params2.isEmpty()) {
                message.fail(404, "Query parameters missing.");
                return;
            }

            preparedQueryMany(sql, params2, ar -> {
                System.out.println("Prepared query switch.");
                if (ar.failed()) {
                    message.fail(500, ar.cause().getMessage());
                    return;
                }
                System.out.println("prepared query success.");
                System.out.println(" prepared query result: " + ar.result());
                message.reply(ar.result());
            });
            break;
        default:
            message.fail(400, "Invalid action.");
            break;
        }
    }

    protected void query(String sql, Handler<AsyncResult<JsonObject>> handler) {
        pool.query(sql, ar -> resultHandler(ar, handler));
    }

    protected void queryMany(String sql, Handler<AsyncResult<JsonArray>> handler) {
        pool.query(sql, ar -> resultHandlerMany(ar, handler));
    }

    protected void preparedQuery(String sql, JsonObject data, Handler<AsyncResult<JsonObject>> handler) {

        final List<Object> params = new ArrayList<>();

        data.forEach(e -> {
            params.add(e.getValue());
        });

        System.out.println("Prepared query function.");
        System.out.println(params);

        pool.preparedQuery(sql, Tuple.wrap(params), ar -> resultHandler(ar, handler));
    }

    protected void preparedQueryMany(String sql, JsonObject data, Handler<AsyncResult<JsonArray>> handler) {

        final List<Object> params = new ArrayList<>();

        data.forEach(e -> {
            params.add(e.getValue());
        });

        System.out.println("Prepared query function many.");
        System.out.println(params);

        pool.preparedQuery(sql, Tuple.wrap(params), ar -> resultHandlerMany(ar, handler));
    }

    private void resultHandler(AsyncResult<RowSet<Row>> ar, Handler<AsyncResult<JsonObject>> handler) {
        if (ar.failed()) {
            ar.cause().printStackTrace();
            handler.handle(Future.failedFuture(ar.cause()));
            return;
        }

        final int numrows = ar.result().rowCount();
        
        if (numrows == 0) {
            handler.handle(Future.failedFuture(new RecordNotFoundException()));
            return;
        } 

        System.out.println("Row count:" + numrows);

        // Row row = ar.result().iterator().next();

        // System.out.println("Row id:" + row.getInteger("id"));

        final JsonObject result = new JsonObject().put("result", ar.result().iterator().next());
        // ar.result().forEach(action);

        // Collector<Row, ?, Map<Long, String>> collector = Collectors

        // System.out.println("AR RESULT:" + result);

        handler.handle(Future.succeededFuture(result));
    }

    private void resultHandlerMany(AsyncResult<RowSet<Row>> ar, Handler<AsyncResult<JsonArray>> handler) {
        if (ar.failed()) {
            ar.cause().printStackTrace();
            handler.handle(Future.failedFuture(ar.cause()));
            return;
        }

        try {
            final JsonArray result = rowsToJson(ar.result());
            handler.handle(Future.succeededFuture(result));
        } catch (RecordNotFoundException e) {
            handler.handle(Future.failedFuture(e));
        }
    }

    protected JsonArray rowsToJson(RowSet<Row> rows) throws RecordNotFoundException {
        final int numrows = rows.rowCount();
        final JsonArray result = new JsonArray();

        if (numrows == 0) {
            throw new RecordNotFoundException();
        } else {
            rows.forEach(row -> {
                result.add(row);
            });
        }

        System.out.println("Rowstojson.");
        System.out.println(result);

        return result;
    }

    protected void getConnection(Handler<AsyncResult<SqlConnection>> handler) {

        pool.getConnection(ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
                handler.handle(Future.failedFuture(ar.cause()));
            }

            handler.handle(Future.succeededFuture(ar.result()));
        });
    }
}