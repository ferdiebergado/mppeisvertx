package com.fsbergado.mppeis.database;

import com.fsbergado.mppeis.models.Model;
import com.fsbergado.mppeis.models.User;
import com.fsbergado.mppeis.utils.TimestampUtil;

import io.vertx.sqlclient.Row;

/**
 * UserMapper
 */
public class UserMapper implements RowMapper {

    @Override
    public Model map(Row row) {
        final User user = new User();
        user.setId(row.getInteger("id"));
        user.setEmail(row.getString("email"));
        user.setRole(row.getInteger("role"));
        user.setIsActive(row.getBoolean("is_active"));
        user.setEmailVerifiedAt(TimestampUtil.format(row.getOffsetDateTime("email_verified_at")));
        user.setCreatedAt(TimestampUtil.format(row.getOffsetDateTime("created_at")));
        user.setUpdatedAt(TimestampUtil.format(row.getOffsetDateTime("updated_at")));
        return user;
    }
}