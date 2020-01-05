package com.fsbergado.mppeis;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fsbergado.mppeis.models.School;
import com.fsbergado.mppeis.utils.TimestampUtil;

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
 * SchoolService
 */
public class SchoolServiceVerticle extends AbstractVerticle {

    public static final String VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE = "school.service.queue";

    private static final String SQL_GET_ALL_SCHOOLS = "SELECT * FROM schools WHERE deleted_at IS NULL";
    private static final String SQL_GET_SCHOOL_BY_ID = "SELECT * FROM schools WHERE id = $1 AND deleted_at IS NULL";
    private static final String SQL_CREATE_A_SCHOOL = "INSERT INTO schools (name, school_id, year_established, school_type, school_location, region_id, division_id, district_id) VALUES ($1, $2, $3, $4, $5, $6, $7, $8) RETURNING id, created_at, updated_at";
    private static final String SQL_UPDATE_A_SCHOOL = "UPDATE schools SET name = $1, year_established = $2, school_type = $3, school_location = $4, region_id = $5, division_id = $6, district_id = $7, updated_at = $8 WHERE id = $9 AND deleted_at IS NULL";
    private static final String SQL_DELETE_A_SCHOOL = "UPDATE schools SET deleted_at = $1 WHERE id = $2 AND deleted_at IS NULL";

    private PgPool pool;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        final PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(Integer.parseInt(System.getenv("PGPORT"))).setHost(System.getenv("PGHOST"))
                .setDatabase(System.getenv("PGDATABASE")).setUser(System.getenv("PGUSER"))
                .setPassword(System.getenv("PGPASSWORD")).setCachePreparedStatements(true);

        // Pool options
        final PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        // Create the pooled client
        pool = PgPool.pool(vertx, connectOptions, poolOptions);     
        
        getConnection(ar -> {
            if (ar.succeeded()) {
                vertx.eventBus().consumer(VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE, this::onMessage);
                System.out.println("School service listening on event bus address: \"" + VERTX_EVENT_BUS_ADDRESS_SCHOOL_SERVICE_QUEUE + "\"...");
                Future.succeededFuture();
            } else {
                ar.cause().printStackTrace();
                System.out.println("Failed starting the service.");
                Future.failedFuture(ar.cause());
            }
        });
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

    public void onMessage(Message<JsonObject> message) {
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
        pool.query(SQL_GET_ALL_SCHOOLS, ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            
            final RowSet<Row> rows = ar.result();
            
            if (rows.rowCount() > 0) {
                JsonArray schools = new JsonArray();
                rows.forEach(row -> {                     
                    School school = mapRowToSchool(row);
                    schools.add(JsonObject.mapFrom(school));
                });
                message.reply(schools);
            }
            message.reply(new JsonObject().put("message", "No schools found."));
        });
    }

    private void show(Message<JsonObject> message) {
        final int id = message.body().getInteger("id");
        pool.preparedQuery(SQL_GET_SCHOOL_BY_ID, Tuple.of(id), ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final RowSet<Row> rows = ar.result();
            if (rows.rowCount() == 1) {                
                School school = mapRowToSchool(rows.iterator().next());
                message.reply(JsonObject.mapFrom(school));
            }
            message.reply(new JsonObject().put("message", "School with id: " + id + " does not exist."));
        });
    }

    private void create(Message<JsonObject> message) {
        final JsonObject data = message.body().getJsonObject("params");
        final List<Object> params = new ArrayList<>();

        data.forEach(e -> params.add(e.getValue()));

        pool.preparedQuery(SQL_CREATE_A_SCHOOL, Tuple.wrap(params), ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final Row row = ar.result().iterator().next();
            final School school = data.mapTo(School.class);
            school.setId(row.getInteger("id"));
            school.setCreatedAt(TimestampUtil.format(row.getOffsetDateTime("created_at")));
            school.setUpdatedAt(TimestampUtil.format(row.getOffsetDateTime("updated_at")));
            message.reply(JsonObject.mapFrom(school));
        });
    }

    private void update(Message<JsonObject> message) {
        final JsonObject body = message.body();
        final int id = body.getInteger("id");

        getConnection(ar -> {
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final SqlConnection conn = ar.result();

            conn.preparedQuery(SQL_GET_SCHOOL_BY_ID, Tuple.of(id), ar2 -> {
                if (ar2.failed()) {
                    conn.close();
                    message.fail(500, ar2.cause().getMessage());
                    return;
                }

                if (ar2.result().rowCount() < 0) {
                    conn.close();
                    message.fail(404, "School with id: " + id + " does not exist.");
                    return;
                }

                final JsonObject data = body.getJsonObject("params");
                final List<Object> params = new ArrayList<>();

                data.forEach(e -> params.add(e.getValue()));
                params.add(OffsetDateTime.now());
                params.add(id);
                
                conn.preparedQuery(SQL_UPDATE_A_SCHOOL, Tuple.wrap(params), ar3 -> {
                    conn.close();
                    if (ar3.failed()) {
                        message.fail(500, ar3.cause().getMessage());
                        return;
                    }
                    message.reply(JsonObject.mapFrom(data));
                });
            });
        });        
    }

    private void destroy(Message<JsonObject> message) {
        getConnection(ar -> { 
            if (ar.failed()) {
                message.fail(500, ar.cause().getMessage());
                return;
            }
            final SqlConnection conn = ar.result();
            final int id = message.body().getInteger("id");

            conn.preparedQuery(SQL_GET_SCHOOL_BY_ID, Tuple.of(id), ar2 -> {
                if (ar2.failed()) {
                    conn.close();
                    message.fail(500, ar2.cause().getMessage());
                    return;
                }
                if (ar2.result().rowCount() == 0) {
                    conn.close();
                    message.fail(404, "School not found.");
                }
                conn.preparedQuery(SQL_DELETE_A_SCHOOL, Tuple.of(OffsetDateTime.now(), id), ar3 -> {
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

    private School mapRowToSchool(Row row) {
        final School school = new School();        

        school.setId(row.getInteger("id"));
        school.setName(row.getString("name"));
        school.setSchoolId(row.getInteger("school_id"));
        school.setYearEstablished(row.getShort("year_established"));
        school.setSchoolType(row.getString("school_type"));
        school.setSchoolLocation(row.getString("school_location"));
        school.setRegionId(row.getShort("region_id"));
        school.setDivisionId(row.getShort("division_id"));
        school.setDistrictId(row.getShort("district_id"));
        school.setCreatedAt(TimestampUtil.format(row.getOffsetDateTime("created_at")));
        school.setUpdatedAt(TimestampUtil.format(row.getOffsetDateTime("updated_at")));
        return school;
    }
}
