package com.example.weatherforecast.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel{
    //关于ViewModel可以管理界面控制器（如 Activity 和 Fragment）的生命周期 ViewModel是用于管理多个Activity或者Fragment数据的类
    private MutableLiveData<String> mText;
    //MutableLiveData的父类是LiveData MutableLiveData则是完全是整个实体类或者数据类型变化后才通知.不会细节到某个字段

    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
