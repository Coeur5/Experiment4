package com.example.weatherforecast.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.weatherforecast.gson.Weather;
import com.example.weatherforecast.util.HttpUtil;
import com.example.weatherforecast.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class AutoUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();//更新天气信息
        updateBingPic();//更新背景图片
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);//定时书中P469
        int anHour = 8 * 60 * 60 * 1000;//8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        //SystemClock.elapsedRealtime()获取从设备boot后经历的时间值
        Intent i = new Intent(this, AutoUpdateService.class);//定时启动
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        //PendingIntent是对Intent的封装，但它不是立刻执行某个行为，而是满足某些条件或触发某些事件后才执行指定的行为
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        //AlarmManager.ELAPSED_REALTIME_WAKEUP表示让定时任务的触发时间从系统开机算起
        //triggerAtTime 定时任务触发时间
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences数据持久化，以键值对形式存储
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.adcodeName;//编码
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=b38826493b8a477eb8c1334f30de6ae2";
            //获取地址
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();//获取响应内容
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);//以键值对方式存储
                        editor.apply();//提交数据 apply没有返回值而commit返回boolean表明修改是否提交成功
                    }
                }
            });
        }
    }

    /**
     *更新背景图片
     */
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();//获取响应内容
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();//提交数据
            }
        });
    }
}
