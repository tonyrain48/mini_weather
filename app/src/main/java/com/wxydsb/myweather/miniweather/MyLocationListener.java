package com.wxydsb.myweather.miniweather;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.wxydsb.myweather.app.MyApplication;
import com.wxydsb.myweather.bean.City;

import java.util.List;

public class MyLocationListener extends BDAbstractLocationListener {
    public String recity;
    public String cityCode;
    @Override
    public void onReceiveLocation(BDLocation location){
        String city=location.getCity();
        city=city.replace("市","");
        city=city.replace("省","");
        Log.d("location_city",city);

        List<City> mCityList;
        MyApplication myApplication;
        myApplication=MyApplication.getInstance();

        mCityList=myApplication.getmCityList();

        for(City cityl:mCityList){
            if(cityl.getCity().equals(city)){
                cityCode=cityl.getNumber();
                Log.d("location_code",cityCode);
            }
        }
    }
}
