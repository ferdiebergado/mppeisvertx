package com.fsbergado.mppeis.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;

/**
 * DBPoolFactory
 */
public class DBManager {

    private final Vertx vertx;
    private PgPool pool;

    public DBManager(Vertx vertx) {
        this.vertx = vertx;
        createPool();
    }

    public void createPool() {
        final PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(Integer.parseInt(System.getenv("PGPORT"))).setHost(System.getenv("PGHOST"))
                .setDatabase(System.getenv("PGDATABASE")).setUser(System.getenv("PGUSER"))
                .setPassword(System.getenv("PGPASSWORD")).setCachePreparedStatements(true);

        // Pool options
        final PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        // Create the pooled client
        pool = PgPool.pool(vertx, connectOptions, poolOptions);        
    }

    public PgPool getPool() {
        return pool;
    }

    public void getConnection(Handler<AsyncResult<SqlConnection>> handler) {

        pool.getConnection(ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
                handler.handle(Future.failedFuture(ar.cause()));
            }

            handler.handle(Future.succeededFuture(ar.result()));
        });
    }    
}