package com.fsbergado.mppeis.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * School
 */
@JsonPropertyOrder({"id", "name", "school_id", "year_established", "school_type", "school_location", "region_id", "division_id", "district_id"})
public class School extends Model {

    private int id;

    private String name;

    @JsonProperty("school_id")
    private int schoolId;

    @JsonProperty("year_established")
    private int yearEstablished;

    @JsonProperty("school_type")
    private String schoolType;

    @JsonProperty("school_location")
    private String schoolLocation;

    @JsonProperty("region_id")
    private int regionId;

    @JsonProperty("division_id")
    private int divisionId;

    @JsonProperty("district_id")
    private int districtId;

    public School() {
        super();
    }

    public int getId() {
        return id;        
    }

    public void setId(int id) {
        this.id = id;        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int id) {
        this.schoolId = id;
    }

    public int getYearEstablished() {
        return yearEstablished;
    }

    public void setYearEstablished(int year) {
        this.yearEstablished = year;
    }

    public String getSchoolType() {
        return schoolType;
    }

    public void setSchoolType(String type) {
        this.schoolType = type;
    }

    public String getSchoolLocation() {
        return schoolLocation;
    }

    public void setSchoolLocation(String location) {
        this.schoolLocation = location;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int id) {
        this.regionId = id;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int id) {
        this.divisionId = id;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int id) {
        this.districtId = id;
    }    
}