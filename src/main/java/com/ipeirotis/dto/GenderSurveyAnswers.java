package com.ipeirotis.dto;

import java.util.Date;

public class GenderSurveyAnswers {
    private Date date;
    private float male;
    private float female;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getMale() {
        return male;
    }

    public void setMale(float male) {
        this.male = male;
    }

    public float getFemale() {
        return female;
    }

    public void setFemale(float female) {
        this.female = female;
    }

}
