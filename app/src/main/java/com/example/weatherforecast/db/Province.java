package com.example.weatherforecast.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {
    private String provinceName;//省名
    private String  provinceCode;//省代号

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String  getProvinceCode() { return provinceCode; }

    public void setProvinceCode(String  provinceCode) {
        this.provinceCode = provinceCode;
    }

}
