package com.example.weatherapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button searchButton;//查找按钮
    private EditText chengShi;//通过城市查询天气
    private Button myConcern;//关注按钮
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chengShi = findViewById(R.id.chengshi_text);
        searchButton = findViewById(R.id.search_button);
        myConcern = findViewById(R.id.concern_text);
        searchButton.setOnClickListener(new View.OnClickListener() {
            //查找按钮监听
            @Override
            public void onClick(View v) {
                String searchCountyCode = String.valueOf(chengShi.getText());//获取输入内容
                if(searchCountyCode.length() != 6){
                    //输入内容不等于6
                    Toast.makeText(MainActivity.this,"城市ID长度为6位!",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                    intent.putExtra("adcode",searchCountyCode);
                    //传递adcode到WeatherActivity
                    startActivity(intent);
                }
            }
        });
        myConcern.setOnClickListener(new View.OnClickListener() {
            //关注按钮监听
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyConcernList.class);
                startActivity(intent);
            }
        });
    }
}