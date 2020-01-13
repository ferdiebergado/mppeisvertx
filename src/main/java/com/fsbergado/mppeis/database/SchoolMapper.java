package com.fsbergado.mppeis.database;

import com.fsbergado.mppeis.models.Model;
import com.fsbergado.mppeis.models.School;
import com.fsbergado.mppeis.utils.TimestampUtil;

import io.vertx.sqlclient.Row;

/**
 * SchoolMapper
 */
public class SchoolMapper implements RowMapper {

    @Override
    public Model map(Row row) {
        final School school = new School();
        school.setId(row.getInteger("id"));
        school.setName(row.getString("name"));
        school.setSchoolId(row.getInteger("school_id"));
        school.setYearEstablished(row.getInteger("year_established"));
        school.setSchoolType(row.getString("school_type"));
        school.setSchoolLocation(row.getString("school_location"));
        school.setRegionId(row.getInteger("region_id"));
        school.setDivisionId(row.getInteger("division_id"));
        school.setDistrictId(row.getInteger("district_id"));
        school.setCreatedAt(TimestampUtil.format(row.getOffsetDateTime("created_at")));
        school.setUpdatedAt(TimestampUtil.format(row.getOffsetDateTime("updated_at")));
        return school;
    }
}