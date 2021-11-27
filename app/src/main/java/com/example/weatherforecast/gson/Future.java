package com.example.weatherforecast.gson;

import com.google.gson.annotations.SerializedName;

public class Future {
    @SerializedName("date")
    public String date;//日期

    @SerializedName("dayweather")
    public String dayweather;//天气情况

    @SerializedName("daytemp")
    public String daytemp;//日间气温

    @SerializedName("nighttemp")
    public String nighttemp;//夜间气温


}
