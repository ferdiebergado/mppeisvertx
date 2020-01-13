package com.fsbergado.mppeis.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * School
 */
@JsonPropertyOrder({"id", "name", "school_id", "year_established", "school_type", "school_location", "region_id", "division_id", "district_id"})
public class School extends Model {

    private Integer id;

    private String name;

    @JsonProperty("school_id")
    private Integer schoolId;

    @JsonProperty("year_established")
    private Integer yearEstablished;

    @JsonProperty("school_type")
    private String schoolType;

    @JsonProperty("school_location")
    private String schoolLocation;

    @JsonProperty("region_id")
    private Integer regionId;

    @JsonProperty("division_id")
    private Integer divisionId;

    @JsonProperty("district_id")
    private Integer districtId;

    public School() {
        super();
    }

    public Integer getId() {
        return id;        
    }

    public void setId(Integer id) {
        this.id = id;        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Integer id) {
        this.schoolId = id;
    }

    public Integer getYearEstablished() {
        return yearEstablished;
    }

    public void setYearEstablished(Integer year_established) {
        this.yearEstablished = year_established;
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

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer id) {
        this.regionId = id;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer id) {
        this.divisionId = id;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer id) {
        this.districtId = id;
    }    
}