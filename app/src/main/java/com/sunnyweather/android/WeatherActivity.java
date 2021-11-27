package com.xh189051009.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xh189051009.weather.gson.Forecast;
import com.xh189051009.weather.gson.Weather;
import com.xh189051009.weather.service.AutoUpdateService;
import com.xh189051009.weather.util.HttpUtil;
import com.xh189051009.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private Button navButton;
    private TextView titleCity;
    private TextView degreeText;
//    private TextView weatherInfoText;
    private ImageView weatherInfoImg;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        degreeText = (TextView) findViewById(R.id.degree_text);
//        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        weatherInfoImg = (ImageView) findViewById(R.id.weather_info_img);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

     //根据天气id请求城市天气信息。
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
//        String weatherUrl="http://api.k780.com/?app=weather.today&weaid="+weatherId.substring(2)+"&appkey=47490&sign=bda2b3359f1a9e9e18d86800ec1d8bd8&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);


//                        if (weather != null && "1".equals(weather.success)) {
//                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
//                            editor.putString("weather", responseText);
//                            editor.apply();
//                            mWeatherId = weather.basic.weatherId;
//                            showWeatherInfo(weather);
//                        } else {
//                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
//                        }
//                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

     //处理并展示Weather实体类中的数据。
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String degree = weather.now.temperature + "℃";
//        String degree = weather.result.temperature_curr + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        degreeText.setText(degree);
        //weatherInfoText.setText(weatherInfo);
        if (weatherInfo.equals("晴")){
        weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.qing) );
        } else if (weatherInfo.equals("多云")){
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.duoyun) );
        } else if (weatherInfo.equals("阴")){
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.yin) );
        } else if (weatherInfo.equals("小雨")){
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.xiaoyu) );
        } else if (weatherInfo.equals("中雨")){
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.zhongyu) );
        } else if (weatherInfo.equals("大雨")){
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.dayu) );
        } else if (weatherInfo.equals("小雪")){
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.xiaoxue) );
        } else{
            weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.xiaoyu) );
        }

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
//            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            ImageView infoImg=(ImageView) view.findViewById(R.id.info_img);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
//            infoImg.setText(forecast.more.info);
            if (forecast.more.info.equals("晴")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.qing) );
            } else if (forecast.more.info.equals("多云")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.duoyun) );
            } else if (forecast.more.info.equals("阴")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.yin) );
            } else if (forecast.more.info.equals("小雨")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.xiaoyu) );
            } else if (forecast.more.info.equals("中雨")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.zhongyu) );
            } else if (forecast.more.info.equals("大雨")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.dayu) );
            } else if (forecast.more.info.equals("小雪")){
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.xiaoxue) );
            } else{
                infoImg.setImageDrawable( getResources().getDrawable(R.drawable.xiaoyu) );
            }
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "出行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);


//        String cityName = weather.result.citynm;
//        String degree = weather.result.temperature + "℃";
//        String weatherInfo = weather.now.more.info;
//        titleCity.setText(cityName);
//        degreeText.setText(degree);
//        //weatherInfoText.setText(weatherInfo);
//        weatherInfoImg.setImageDrawable( getResources().getDrawable(R.drawable.test) );
//        forecastLayout.removeAllViews();
//        for (Forecast forecast : weather.forecastList) {
//            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
//            TextView dateText = (TextView) view.findViewById(R.id.date_text);
//            TextView infoText = (TextView) view.findViewById(R.id.info_text);
//            TextView maxText = (TextView) view.findViewById(R.id.max_text);
//            TextView minText = (TextView) view.findViewById(R.id.min_text);
//            dateText.setText(forecast.date);
//            infoText.setText(forecast.more.info);
//            maxText.setText(forecast.temperature.max);
//            minText.setText(forecast.temperature.min);
//            forecastLayout.addView(view);
//        }
//        if (weather.aqi != null) {
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
//        String comfort = "舒适度：" + weather.suggestion.comfort.info;
//        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
//        String sport = "运行建议：" + weather.suggestion.sport.info;
//        comfortText.setText(comfort);
//        carWashText.setText(carWash);
//        sportText.setText(sport);
//        weatherLayout.setVisibility(View.VISIBLE);
//        Intent intent = new Intent(this, AutoUpdateService.class);
//        startService(intent);
    }
}
