package com.example.weatherapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MyConcernList extends AppCompatActivity {
    ArrayAdapter simpleAdapter;
    ListView MyConcernList;
    private List<String> city_nameList = new ArrayList<>();//城市名字列表
    private List<String> city_codeList = new ArrayList<>();//城市编码列表
    //初始化
    private void InitConcern() {
        //进行数据填装
        MyDBhelper dbHelper = new MyDBhelper(this,DB_NAME,null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.rawQuery("select * from Concern",null);
        //建立关注数据库游标
        while(cursor.moveToNext()){
            //游标从头移动到数据表最后一行
            String city_code = cursor.getString(cursor.getColumnIndex("city_code"));
            String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
            city_codeList.add(city_code);//将数据库城市编码放入列表
            city_nameList.add(city_name);//将数据库城市名放入列表
        }
    }

    public void RefreshList(){
        //刷新列表
        city_nameList.removeAll(city_nameList);//将列表清空
        city_codeList.removeAll(city_codeList);
        simpleAdapter.notifyDataSetChanged();
        //notifyDataSetChanged方法强制listview调用getView来刷新每个Item的内容
        MyDBhelper dbHelper = new MyDBhelper(this,DB_NAME,null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.rawQuery("select * from Concern",null);
        while(cursor.moveToNext()){
            String city_code = cursor.getString(cursor.getColumnIndex("city_code"));
            String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
            city_codeList.add(city_code);//将数据库城市编码放入列表
            city_nameList.add(city_name);//将数据库城市名放入列表
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        RefreshList();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myconcern_list);
        MyConcernList = findViewById(R.id.MyConcernList);

        InitConcern();//初始化

        simpleAdapter = new ArrayAdapter(MyConcernList.this,android.R.layout.simple_list_item_1,city_nameList);
        //listview显示
        MyConcernList.setAdapter(simpleAdapter);
        MyConcernList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            //配置ArrayList点击按钮
            @Override
            public void  onItemClick(AdapterView<?> parent, View view , int position , long id){
                String tran = city_codeList.get(position);
                Intent intent = new Intent(MyConcernList.this, WeatherActivity.class);
                intent.putExtra("adcode",tran);//传递城市编码到 WeatherActivity
                startActivity(intent);
            }
        });

    }

}
