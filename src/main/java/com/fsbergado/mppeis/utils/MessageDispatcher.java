package com.fsbergado.mppeis.utils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fsbergado.mppeis.database.DBManager;
import com.fsbergado.mppeis.database.RowMapper;
import com.fsbergado.mppeis.models.Model;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

/**
 * MessageDispatcher
 */
public class MessageDispatcher implements Handler<Message<JsonObject>> {

    private String SQL_GET_ALL_RESOURCE;
    private String SQL_GET_RESOURCE_BY_ID;
    private String SQL_DELETE_RESOURCE;
    private final String SQL_SELECT = "SELECT * FROM ";
    private final String DELETED_AT_IS_NULL = "deleted_at IS NULL";
    private final String INITIAL_QUERY;

    private final DBManager db;
    private final PgPool pool;
    private final String table;
    private Model model;
    private final RowMapper mapper;

    public MessageDispatcher(DBManager db, String table, Model model, RowMapper mapper) {
        this.db = db;
        this.pool = db.getPool();
        this.table = table;
        this.model = model;
        this.mapper = mapper;
        this.INITIAL_QUERY = SQL_SELECT + table;
        setQueryStrings();
    }

    private void setQueryStrings() {
        SQL_GET_ALL_RESOURCE = INITIAL_QUERY + " WHERE " + DELETED_AT_IS_NULL;
        SQL_GET_RESOURCE_BY_ID = INITIAL_QUERY + " WHERE id = $1 AND " + DELETED_AT_IS_NULL;
        SQL_DELETE_RESOURCE = "UPDATE " + table + " SET deleted_at = $1 WHERE id = $2 AND " + DELETED_AT_IS_NULL;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        if (!message.headers().contains("action")) {
            message.fail(404, "No action header specified");
            return;
        }

        final String action = message.headers().get("action");

        switch (action) {
        case "index":
            index(message);
            break;
        case "show":
            show(message);
            break;
        case "create":
            create(message);
            break;
        case "update":
            update(message);
            break;
        case "destroy":
            destroy(message);
            break;
        default:
            break;
        }
    }

    private void index(Message<JsonObject> message) {
        pool.query(SQL_GET_ALL_RESOURCE, ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }

            final RowSet<Row> rows = ar.result();

            if (rows.rowCount() > 0) {
                JsonArray resources = new JsonArray();
                rows.forEach(row -> {
                    model = mapper.map(row);
                    resources.add(JsonObject.mapFrom(model));
                });
                message.reply(resources);
            }
            message.reply(new JsonObject().put("message", "No resource found."));
        });
    }

    private void show(Message<JsonObject> message) {
        final int id = message.body().getInteger("id");
        pool.preparedQuery(SQL_GET_RESOURCE_BY_ID, Tuple.of(id), ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final RowSet<Row> rows = ar.result();
            if (rows.rowCount() == 1) {
                model = mapper.map(rows.iterator().next());
                message.reply(JsonObject.mapFrom(model));
            }
            message.reply(new JsonObject().put("message", "Resource with id: " + id + " does not exist."));
        });
    }

    private void create(Message<JsonObject> message) {
        final JsonObject data = message.body().getJsonObject("params");
        final String query1 = "INSERT INTO " + table + " (";
        final String query2 = String.join(", ", data.fieldNames());
        final String query3 = ") VALUES (";
        final int datalength = data.size();
        String query4 = "";

        for (int i = 1; i <= datalength; i++) {
            query4 += "$" + i;
            if (i != datalength)
                query4 += ", ";
        }

        final String query5 = ") RETURNING id, created_at, updated_at";
        final String sql = query1 + query2 + query3 + query4 + query5;
        final List<Object> params = new ArrayList<>();

        data.forEach(e -> params.add(e.getValue()));

        pool.preparedQuery(sql, Tuple.wrap(params), ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final Row row = ar.result().iterator().next();
            model = data.mapTo(model.getClass());
            // model.setId(row.getInteger("id"));
            model.setCreatedAt(TimestampUtil.format(row.getOffsetDateTime("created_at")));
            model.setUpdatedAt(TimestampUtil.format(row.getOffsetDateTime("updated_at")));
            message.reply(JsonObject.mapFrom(model));
        });
    }

    private void update(Message<JsonObject> message) {
        final JsonObject body = message.body();
        final int id = body.getInteger("id");

        db.getConnection(ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final SqlConnection conn = ar.result();

            // Check if resource exists
            conn.preparedQuery(SQL_GET_RESOURCE_BY_ID, Tuple.of(id), ar2 -> {
                if (ar2.failed()) {
                    conn.close();
                    message.fail(500, ar2.cause().getMessage());
                    return;
                }

                // Resource does not exist
                if (ar2.result().rowCount() < 0) {
                    conn.close();
                    message.fail(404, "Resource with id: " + id + " does not exist.");
                    return;
                }

                // Build the update query statement
                final JsonObject data = body.getJsonObject("params");
                final String query1 = "UPDATE " + table +  " SET ";
                final Set<String> fields = data.fieldNames();
                int ctr = 1;
                String query2 = "";

                for (String field : fields) {
                    query2 += field + " = $" + ctr + ", ";
                    ctr++;
                }

                final String query3 = "updated_at = $" + ctr + " WHERE id = $" + (ctr + 1);

                // The update query string
                final String sql = query1 + query2 + query3;

                // The update query parameters
                final List<Object> params = new ArrayList<>();

                // Build the update query parameters
                data.forEach(e -> params.add(e.getValue()));
                params.add(OffsetDateTime.now());
                params.add(id);

                // Perform the update
                conn.preparedQuery(sql, Tuple.wrap(params), ar3 -> {
                    conn.close();
                    if (ar3.failed()) {
                        message.fail(500, ar3.cause().getMessage());
                        return;
                    }
                    // Send the updated data
                    message.reply(JsonObject.mapFrom(data));
                });
            });
        });
    }

    private void destroy(Message<JsonObject> message) {
        db.getConnection(ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final SqlConnection conn = ar.result();
            final int id = message.body().getInteger("id");

            conn.preparedQuery(SQL_GET_RESOURCE_BY_ID, Tuple.of(id), ar2 -> {
                if (ar2.failed()) {
                    conn.close();
                    message.fail(500, ar2.cause().getMessage());
                    return;
                }
                if (ar2.result().rowCount() == 0) {
                    conn.close();
                    message.fail(404, "School not found.");
                }
                conn.preparedQuery(SQL_DELETE_RESOURCE, Tuple.of(OffsetDateTime.now(), id), ar3 -> {
                    conn.close();
                    if (ar3.failed()) {
                        message.fail(500, ar3.cause().getMessage());
                        return;
                    }
                    message.reply(new JsonObject());
                });
            });
        });
    }
}