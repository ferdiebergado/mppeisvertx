package com.fsbergado.mppeis.utils;

import com.fsbergado.mppeis.database.DBManager;
import com.fsbergado.mppeis.database.RowMapper;
import com.fsbergado.mppeis.models.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

/**
 * AuthDispatcher
 */
public class AuthDispatcher implements Handler<Message<JsonObject>> {

    final DBManager db;
    final RowMapper mapper;
    final PgPool pool;

    public AuthDispatcher(DBManager db, RowMapper mapper) {
        this.db = db;
        this.pool = db.getPool();
        this.mapper = mapper;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        if (!message.headers().contains("action")) {
            message.fail(404, "No action header specified");
            return;
        }

        final String action = message.headers().get("action");

        switch (action) {
        case "register":
            register(message);
            break;
        case "login":
            login(message);
            break;
        }
    }

    private void register(Message<JsonObject> message) {
        final String email = message.body().getString("email");
        final String password = message.body().getString("password");
        final String password_confirmation = message.body().getString("password_confirmation");

        if (Validator.validateEmail(email)) {
            if (password.equals(password_confirmation)) {
                // Create an encoder with strength 16
                final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(16);
                final String hashed = encoder.encode(password);
                pool.preparedQuery("INSERT INTO users (email, password) VALUES ($1, $2) RETURNING id, created_at, updated_at", Tuple.of(email, hashed), ar -> {
                    if (ar.failed()) {
                        message.fail(500, ar.cause().getMessage());
                        return;
                    }
                    final Row row = ar.result().iterator().next();
                    User user = mapper.map(row);

                    message.reply(JsonObject.mapFrom(model));

                });

            }
        }
    }
    private void login(Message<JsonObject> message) {

        final String email = message.body().getString("email");
    }


    
}