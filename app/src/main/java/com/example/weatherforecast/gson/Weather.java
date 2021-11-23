package com.example.weatherforecast.gson;
import com.google.gson.annotations.SerializedName;
public class Weather {
    //@SerializedName注解来将对象里的属性跟json里字段对应值匹配起来
    @SerializedName("province")
    public String provinceName;//省名称

    @SerializedName("city")
    public String cityName;//城市名称

    @SerializedName("adcode")
    public String adcodeName;//编码

    @SerializedName("weather")
    public String weatherName;//天气情况

    @SerializedName("temperature")
    public String temperatureName;//温度

    @SerializedName("winddirection")
    public String windDirection;//风向

    @SerializedName("windpower")
    public String windPower;//风力

    @SerializedName("humidity")
    public String humidityName;//湿度

    @SerializedName("reporttime")
    public String reportTimeName;//时间

}
