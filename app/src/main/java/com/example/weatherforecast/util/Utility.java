package com.example.weatherforecast.util;
import android.text.TextUtils;

import com.example.weatherforecast.gson.Future;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.weatherforecast.db.City;
import com.example.weatherforecast.db.County;
import com.example.weatherforecast.db.Province;
import com.example.weatherforecast.gson.Weather;

import java.util.ArrayList;
import java.util.List;


public class Utility {
    //解析和处理从服务器返回的省市县数据
    public static boolean handleProvinceResponse(String response){
        //解析和处理服务器返回的省级数据
        //解析的规则就是先按逗号分隔，再按单竖线分隔，接着将解析出来的数据设置到实体类中，最后调用save()方法将数据存储到相应的表中。
        if (!TextUtils.isEmpty(response)) {
            //响应返回不空
            try {
                JSONObject jsonObject = new JSONObject(response);
                //把响应转换成json对象
                JSONArray countryAll = jsonObject.getJSONArray("districts");
                // json数组，使用中括号[ ],只不过数组里面的项也是json键值对格式的
                for (int i = 0; i < countryAll.length(); i++) {
                    JSONObject countryLeve0 = countryAll.getJSONObject(i);
                    //插入省
                    JSONArray provinceAll = countryLeve0.getJSONArray("districts");
                    for (int j = 0; j < provinceAll.length(); j++) {
                        JSONObject province1 = provinceAll.getJSONObject(j);
                        String adcode1 = province1.getString("adcode");//获取网页中adcode标签的值
                        String name1 = province1.getString("name");//获取网页中name标签的值
                        Province provinceN = new Province();
                        provinceN.setProvinceCode(adcode1);//放入编码
                        provinceN.setProvinceName(name1);//设置省名
                        provinceN.save();//放入数据库
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, String provinceCode){
        //解析和处理服务器返回的市级数据
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray provinceAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < provinceAll.length(); i++) {
                    JSONObject province1 = provinceAll.getJSONObject(i);
                    //插入市
                    JSONArray cityAll = province1.getJSONArray("districts");
                    for (int j = 0; j < cityAll.length(); j++) {
                        JSONObject city2 = cityAll.getJSONObject(j);
                        String adcode2 = city2.getString("adcode");//获取网页中adcode标签的值
                        String name2 = city2.getString("name");//获取网页中name标签的值
                        City cityN = new City();
                        cityN.setCityCode(adcode2);
                        cityN.setCityName(name2);
                        cityN.setProvinceCode(provinceCode);//设置该市所属省的编码
                        cityN.save();//存入数据库
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, String cityCode){
        //解析和处理服务器返回的区级数据
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray cityAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < cityAll.length(); i++) {
                    JSONObject city2 = cityAll.getJSONObject(i);
                    //插入市
                    JSONArray countyAll = city2.getJSONArray("districts");
                    for (int j = 0; j < countyAll.length(); j++) {
                        JSONObject county3 = countyAll.getJSONObject(j);
                        String adcode3 = county3.getString("adcode");//获取网页中adcode标签的值
                        String name3 = county3.getString("name");//获取网页中name标签的值
                        County countyN = new County();
                        countyN.setCountyCode(adcode3);//设置县的编码
                        countyN.setCountyName(name3);//设置县名
                        countyN.setCityCode(cityCode);//设置该县所属市的编码
                        countyN.save();//存入数据库
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            //JSONObject只是一种数据结构，可以理解为JSON格式的数据结构（key-value 结构）
            JSONArray jsonArray = jsonObject.getJSONArray("lives");//将网页里lives的数据取出来
            //json数组，使用中括号[ ],只不过数组里面的项也是json键值对格式的
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject x = jsonArray.getJSONObject(i);
                String weatherContent = x.toString();//获取json字符串
                return new Gson().fromJson(weatherContent, Weather.class);
                //Gson提供了fromJson()方法来实现从Json相关对象到Java实体的方法

            } }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Future> handleFutureWeatherResponse(String response) {
        List<Future> futureList =new ArrayList<Future>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            //JSONObject只是一种数据结构，可以理解为JSON格式的数据结构（key-value 结构）
            JSONArray jsonArray = jsonObject.getJSONArray("forecasts");
            JSONObject getJsonObj = jsonArray.getJSONObject(0);//获取json数组中的第一项
            JSONArray jsonArray1=getJsonObj.getJSONArray("casts");

            //json数组，使用中括号[ ],只不过数组里面的项也是json键值对格式的
            for(int i=0; i<jsonArray1.length(); i++) {
                JSONObject x = jsonArray1.getJSONObject(i);
                String FutureweatherContent = x.toString();//获取json字符串*/
                Future future=new Gson().fromJson(FutureweatherContent, Future.class);
                futureList.add(future);

                //Gson提供了fromJson()方法来实现从Json相关对象到Java实体的方法
            }}catch (Exception e) {
            e.printStackTrace();
        }
        return futureList;
    }

}
