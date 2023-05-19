package com.devtwist.mymessenger.Models;

public class CountryCodeData {

    private String countryName, countryCode;

    public CountryCodeData() {
    }

    public CountryCodeData(String countryName, String countryCode) {
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
