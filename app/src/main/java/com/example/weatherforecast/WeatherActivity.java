package com.example.weatherforecast;

import static com.example.weatherforecast.MyDBhelper.DB_NAME;
import static com.example.weatherforecast.MyDBhelper.TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.R;
import com.example.weatherforecast.gson.Future;
import com.example.weatherforecast.gson.Weather;
import com.example.weatherforecast.util.HttpUtil;
import com.example.weatherforecast.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;//菜单
    private Button navButton;//选择城市按钮
    private Button concern;//关注
    private Button concealConcern;//取消关注
    private Button goBack;//返回
    private Button refresh;//刷新
    public SwipeRefreshLayout swipeRefresh; //下拉刷新
    private ScrollView weatherLayout;//滚动
    private ImageView bingPicImg;//图片
    private TextView provinceText;//省区
    private TextView cityText;//市区
    private TextView weatherText;//天气
    private TextView temperatureText;//温度
    private TextView humidityText;//湿度
    private TextView reportTimeText;//时间
    private TextView winddirection;//风向
    private LinearLayout futureLayout;//未来天气

    String countyCode;//县编码
    String countyName;//县名

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            //判断版本
            View decorView = getWindow().getDecorView();
            //获取DecorView实例控件
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //活动布局显示在状态栏上
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏透明
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = findViewById(R.id.weather_layout);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        provinceText = findViewById(R.id.province_text);
        cityText = findViewById(R.id.city_text);
        weatherText = findViewById(R.id.weather_text);
        temperatureText = findViewById(R.id.temperature_text);
        humidityText = findViewById(R.id.humidity_text);
        winddirection=findViewById(R.id.winddirection_text);
        reportTimeText = findViewById(R.id.reporttime_text);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        concern = findViewById(R.id.concern);
        concealConcern = findViewById(R.id.concealConcern);
        goBack = findViewById(R.id.goBack);
        refresh = findViewById(R.id.refresh);
        futureLayout=(LinearLayout)findViewById(R.id.future_layout);

        SharedPreferences prefs = getSharedPreferences(String.valueOf(this),MODE_PRIVATE);
        String adcodeString = prefs.getString("weather",null);

        if (adcodeString != null) {
            Weather weather = Utility.handleWeatherResponse(adcodeString);
            //根据adcodeString 返回weather对象
            countyCode = weather.adcodeName;
            countyName = weather.cityName;
            showWeatherInfo(weather);
            //显示天气情况
        } else {
            countyCode = getIntent().getStringExtra("adcode");
            //获取从ChooseAreaFragment传递的参数
            //countyName = getIntent().getStringExtra("city");
            weatherLayout.setVisibility(View.INVISIBLE);
            //View.INVISIBLE--->不可见，但这个View仍然会占用在xml文件中所分配的布局空间，不重新layout
            futureLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyCode);
            //获取县天气
            requestfutureWeather(countyCode);
        }
        final String x = cityText.getText().toString();
        //获取当前位置
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            //下拉进度条监听器
            @Override
            public void onRefresh() {
                requestWeather(countyCode);//回调方法
            }
        });
        navButton.setOnClickListener(new View.OnClickListener(){
            //点击监听
            @Override
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
            }
            //GravityCompat.START : 左边菜单打开
        });
        concern.setOnClickListener(new View.OnClickListener() {
            //关注按钮点击监听
            @Override
            public void onClick(View v) {
                MyDBhelper dbHelper = new MyDBhelper(WeatherActivity.this, DB_NAME, null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();//以写的方式打开数据库
                ContentValues values = new ContentValues();//Contentvalues却只能存储基本类型的数据
                values.put("city_code", countyCode);//放入县编码
                values.put("city_name", countyName);//放入县名
                db.insert(TABLE_NAME, null, values);//插入数据库
                //TABLE_NAME="Concern"
                Toast.makeText(WeatherActivity.this, "关注成功！", Toast.LENGTH_LONG).show();//提示信息
            }
        });
        concealConcern.setOnClickListener(new View.OnClickListener() {
            //取消关注按钮点击监听
            @Override
            public void onClick(View v) {
                MyDBhelper dbHelper = new MyDBhelper(WeatherActivity.this, DB_NAME, null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();//以写的方式打开数据库
                db.delete(TABLE_NAME,"city_code=?",new String[]{String.valueOf(countyCode)});
                //按编码筛选条件删除
                Toast.makeText(WeatherActivity.this, "取消关注成功！", Toast.LENGTH_LONG).show();
            }
        });
        goBack.setOnClickListener(new View.OnClickListener(){
            //返回按钮监听
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                //跳转至MainActivity
                startActivity(intent);
                finish();//finish WeatherActivity
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            //刷新按钮监听
            @Override
            public void onClick(View v) {
                requestWeather(countyCode);//调用天气请求
                requestfutureWeather(countyCode);//调用未来天气请求
            }
        });
        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
            //Glide图片加载库，下载bingPic，显示到bingPicImg
        }else {
            loadBingPic();
        }

    }

    public void requestWeather(final String adCode) {
        //请求获取天气信息
        String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + adCode + "&key=c1894e9fcaf35e9fceabe9afaf40d45f";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //响应
                final String responseText = response.body().string();
                //获取网页响应的主体
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null) {
                            countyName=weather.cityName;//放入城市名
                            countyCode=adCode;//放入城市编码
                            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(this),MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);//放入数据
                            editor.apply();//提交数据
                            showWeatherInfo(weather);//显示响应返回信息
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败,城市ID不存在，请重新输入！", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                        //取消动画就调用setRefreshing（false）
                    }
                });
                loadBingPic();//下载图片
            }

            @Override
            public void onFailure(Call call, IOException e) {
                //获取天气信息失败，抛出异常
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
        loadBingPic();
    }
    /*询问未来天气*/
    public void requestfutureWeather(final String adCode){
        //请求获取天气信息
        String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + adCode + "&extensions=all&key=c1894e9fcaf35e9fceabe9afaf40d45f";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //响应
                final String responseText = response.body().string();
                //获取网页响应的主体
                final List<Future> futureweather = Utility.handleFutureWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (futureweather != null) {
                            //countyName=futureweather.cityName;
                            countyCode=adCode;
                            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(this),MODE_PRIVATE).edit();
                            editor.putString("futureweather", responseText);//放入数据
                            editor.apply();//提交数据
                            showWeatherInfo1(futureweather);//显示响应返回信息
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败,城市ID不存在，请重新输入！", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                        //取消动画就调用setRefreshing（false）
                    }
                });

            }
            @Override
            public void onFailure(Call call, IOException e) {
                //获取天气信息失败，抛出异常
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取未来天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();

    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";//图片下载地址
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            //获取失败 抛出异常
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
    /*显示天气*/
    private void showWeatherInfo(Weather weather) {
        String provinceName = weather.provinceName;//省名
        String cityName = weather.cityName;//市名
        String weatherName = weather.weatherName;//天气名
        String temperatureName = weather.temperatureName;//温度
        String humidityName = weather.humidityName;//湿度
        String reportTime = weather.reportTimeName;//时间
        String winddirectionName=weather.windDirection;
        provinceText.setText(provinceName);//显示省名
        cityText.setText(cityName);//显示市名
        weatherText.setText("天气:" + weatherName);
        temperatureText.setText("温度:" + temperatureName + "℃");
        humidityText.setText("湿度:" + humidityName + "%");
        winddirection.setText("风向:"+winddirectionName);
        reportTimeText.setText(reportTime);
        weatherLayout.setVisibility(View.VISIBLE);

    }
    /*显示未来天气*/
    private void showWeatherInfo1(List<Future> futureweather){
        futureLayout.removeAllViews();//清除存储原有的查询结果的组件

        for(Future future:futureweather){
            View view= LayoutInflater.from(this).inflate(R.layout.future_item,futureLayout,false);
            //inflate这个函数的返回值就是获取的布局文件,将future_item放入futureLayout显示
            TextView dateText=(TextView)view.findViewById(R.id.future_date);
            TextView dayweatherText=(TextView)view.findViewById(R.id.future_dayweather);
            TextView daytempText=(TextView)view.findViewById(R.id.future_daytemp);
            TextView nighttempText=(TextView)view.findViewById(R.id.future_nighttemp);

            dateText.setText(future.date);//时间
            dayweatherText.setText(future.dayweather);//天气情况
            daytempText.setText(future.daytemp+"℃");//日间温度
            nighttempText.setText(future.nighttemp+"℃");//夜间温度
            futureLayout.addView(view);
            futureLayout.setVisibility(View.VISIBLE);
            //View.VISIBLE显示
        }
    }
}
