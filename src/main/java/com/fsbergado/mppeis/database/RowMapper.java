package com.fsbergado.mppeis.database;

import com.fsbergado.mppeis.models.Model;

import io.vertx.sqlclient.Row;

/**
 * RowMapper
 */
public interface RowMapper {

    public Model map(Row row);
}