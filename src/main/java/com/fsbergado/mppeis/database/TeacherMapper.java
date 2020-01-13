package com.fsbergado.mppeis.database;

import com.fsbergado.mppeis.models.Model;
import com.fsbergado.mppeis.models.Teacher;
import com.fsbergado.mppeis.utils.TimestampUtil;

import io.vertx.sqlclient.Row;

/**
 * TeacherMapper
 */
public class TeacherMapper implements RowMapper {

    @Override
    public Model map(Row row) {
        final Teacher teacher = new Teacher();
        teacher.setId(row.getInteger("id"));
        teacher.setLastName(row.getString("last_name"));
        teacher.setFirstName(row.getString("first_name"));
        teacher.setMi(row.getString("mi"));
        teacher.setSex(row.getString("sex"));
        teacher.setPositionId(row.getInteger("position_id"));
        teacher.setSchoolId(row.getInteger("school_id"));
        teacher.setCreatedAt(TimestampUtil.format(row.getOffsetDateTime("created_at")));
        teacher.setUpdatedAt(TimestampUtil.format(row.getOffsetDateTime("updated_at")));
        return teacher;
    }
}