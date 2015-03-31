package com.ipeirotis.dto;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class ByCountryAnswers {

    private Map<Date, Map<String, Float>> data;
    private Set<String> countries;

    public Map<Date, Map<String, Float>> getData() {
        return data;
    }

    public void setData(Map<Date, Map<String, Float>> data) {
        this.data = data;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }
}
