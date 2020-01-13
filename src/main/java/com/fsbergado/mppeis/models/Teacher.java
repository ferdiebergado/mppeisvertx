package com.fsbergado.mppeis.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Teacher
 */
@JsonPropertyOrder({"id", "last_name", "first_name", "mi", "sex", "position_id", "school_id"})
public class Teacher extends Model {

    private Integer id;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name")
    private String firstName;

    private String mi;

    private String sex;

    @JsonProperty("position_id")
    private Integer positionId;

    @JsonProperty("school_id")
    private Integer schoolId;

    public Teacher() {
        super();
    }

    public Integer getId() {
        return id;        
    }

    public void setId(Integer id) {
        this.id = id;        
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstname) {
        this.firstName = firstname;
    }

    public String getMi() {
        return mi;
    }

    public void setMi(String mi) {
        this.mi = mi;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }    

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer id) {
        this.positionId = id;
    }

    public Integer getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Integer id) {
        this.schoolId = id;
    }
}