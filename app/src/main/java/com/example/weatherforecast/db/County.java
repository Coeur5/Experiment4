package com.example.weatherforecast.db;

import org.litepal.crud.LitePalSupport;

public class County extends LitePalSupport {

    private String countyName;//区名
    private String  countyCode;//区代号
    private String  cityCode;//市代号


    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String  getCityCode(){
        return cityCode;
    }

    public void setCityCode(String  cityCode){
        this.cityCode = cityCode;
    }

    public void setCountyCode(String  countyCode){
        this.countyCode = countyCode;
    }

    public String   getCountyCode(){
        return countyCode;
    }


}
