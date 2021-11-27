package com.example.weatherforecast;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weatherforecast.MainActivity;
import com.example.weatherforecast.MyConcernList;
import com.example.weatherforecast.R;
import com.example.weatherforecast.WeatherActivity;
import com.example.weatherforecast.db.City;
import com.example.weatherforecast.db.County;
import com.example.weatherforecast.db.Province;
import com.example.weatherforecast.util.HttpUtil;
import com.example.weatherforecast.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE = 0;//省级别
    private static final int LEVEL_CITY = 1;//市级别
    private static final int LEVEL_COUNTY = 2;//区级别
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter; // ListView适配器
    private TextView titleText;//显示当前位置
    private Button backButton;//返回按钮
    private Button concernButton;//关注按钮
    private ListView listView;//列表
    private Button searchButton;//查询按钮
    private EditText chengShi;


    private List<Province> provinceList;//省列表
    private List<City> cityList;//城市列表
    private List<County> countyList;//城镇列表
    private int currentLevel;//当前等级
    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        concernButton=view.findViewById(R.id.concern_text);
        listView = view.findViewById(R.id.list_view);
        searchButton=view.findViewById(R.id.search_button);
        chengShi=view.findViewById(R.id.chengshi_text);

        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        //listview适配器，simple_list_item_1 android自带，datalist为listview要显示的内容
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    //当前在省级别
                    selectedProvince = provinceList.get(position);
                    //获取选中省份的位置信息
                    queryCities();
                    //查找该省份中所有城市
                } else if (currentLevel == LEVEL_CITY) {
                    //当前在市级别
                    selectedCity = cityList.get(position);
                    //获取选中城市信息
                    queryCounties();
                    //查找该城市中所有区
                } else if (currentLevel == LEVEL_COUNTY) {
                    //当前在县级别
                    String countyCode = countyList.get(position).getCountyCode();
                    //获取点击位置的县编码
                    String countyName = countyList.get(position).getCountyName();
                    //获取点击位置县名
                    if (getActivity() instanceof MainActivity) {
                        //判断context是否属于MainActivity
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        //跳转到WeatherActivity
                        intent.putExtra( "adcode",countyCode);//设置跳转需要传递的 县编码
                        intent.putExtra("city",countyName);//置跳转需要传递的 县名
                        startActivity(intent);
                        getActivity().finish();//关闭当前activity
                    } else if (getActivity() instanceof WeatherActivity) {
                        //在WeatherActivity活动中  左侧碎片调用
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        //关闭滑动菜单
                        activity.swipeRefresh.setRefreshing(true);
                        //下拉刷新进度条
                        activity.requestWeather(countyCode);
                        //调用WeatherActivity的requestWeather方法 传递县编码作为参数 查找天气情况
                        activity.requestfutureWeather(countyCode);
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            //返回按钮监听
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    //如果当前再县级
                    queryCities();//返回到市级
                }else if (currentLevel == LEVEL_CITY){
                    //当前在市级
                    queryProvinces();//返回到省级
                }
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            //查找按钮监听
            @Override
            public void onClick(View v) {
                String searchCountyCode = String.valueOf(chengShi.getText());//获取输入内容
                if(searchCountyCode.length() != 6){
                    //输入内容不等于6
                    Toast.makeText(getActivity(),"城市ID长度为6位!",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("adcode",searchCountyCode);
                    //传递adcode到WeatherActivity
                    startActivity(intent);
                }
            }
        });
        concernButton.setOnClickListener(new View.OnClickListener(){
            //关注按钮
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MyConcernList.class);
                startActivity(intent);
            }
        });
        queryProvinces();
    }



    /**
     * 查询所有的省，优先从数据库查询，如果没有查到再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        // 省级为最底层，无法再次返回 View.GONE是不可见的，不占用原来的布局空间
        provinceList = LitePal.findAll(Province.class);//获取省数据，放入列表
        if (provinceList.size()>0){
            //如果列表大于0
            dataList.clear();//清空数据列表
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());//数据列表添加省名
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //第position个item显示在listView的最上面一项
            currentLevel = LEVEL_PROVINCE;
            //LEVEL_PROVINCE=0,当前在省级
        }else{
            //服务器查询
            String address = "https://restapi.amap.com/v3/config/district?keywords=中国&subdistrict=1&key=c1894e9fcaf35e9fceabe9afaf40d45f";
            queryFromServer(address,"province");
        }
    }
    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());//获取省名
        backButton.setVisibility(View.VISIBLE);
        //VISIBLE:0  意思是可见的
        cityList = LitePal.where("provinceCode = ?",
                String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        //根据省编码查询
        //数据库查询
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());//添加城市名字
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;//设置当前登记为市级
        }else {
            //如果不再数据库里则使用api访问
            String provinceName = selectedProvince.getProvinceName();
            String address = "https://restapi.amap.com/v3/config/district?keywords="+provinceName+"&subdistrict=1&key=c1894e9fcaf35e9fceabe9afaf40d45f";
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityCode=?",
                String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size() >0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;//设置当前登记为县级
        }else{
            //如果不再数据库里则使用api访问
            String cityName = selectedCity.getCityName();
            String address = "https://restapi.amap.com/v3/config/district?keywords="+cityName+"&subdistrict=1&key=c1894e9fcaf35e9fceabe9afaf40d45f";
            queryFromServer(address,"county");
        }
    }


    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        //提示信息
                    }
                });
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();//获取响应内容
                boolean result = false;
                if("province".equals(type)){
                    //如果查询的是省
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    //查询的是市
                    result = Utility.handleCityResponse(responseText,selectedProvince.getProvinceCode());
                }else if("county".equals(type)){
                    //查询的是县
                    result = Utility.handleCountyResponse(responseText,selectedCity.getCityCode());
                }
                if(result){
                    //result=true
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvinces();//查询省
                            }else if("county".equals(type)){
                                queryCities();//查询市
                            }else if("county".equals(type)){
                                queryCounties();//查询县
                            }
                        }
                    });
                }
            }
        });
    }
}
