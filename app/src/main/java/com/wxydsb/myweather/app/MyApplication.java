package com.wxydsb.myweather.app;

import android.app.Application;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import com.wxydsb.myweather.bean.City;
import com.wxydsb.myweather.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//最长维持的生命周期，保证打开app时能读到db
public class MyApplication extends Application{
    private  static  final String TAG="MyAPP";

    private static MyApplication mApplication;
    private CityDB mCityDB;

    private List<City> mCityList;

//    初始化，打开数据库
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication=this;
        mCityDB=openCityDB();
        initCityList();
    }

    private void initCityList(){
        mCityList=new ArrayList<City>();
        new Thread(new Runnable(){
            public void run(){
                prepareCityList();
            }
        }).start();
    }

//    读city列表
    private boolean prepareCityList(){
        mCityList=mCityDB.getAllCity();
        int i=0;
        for (City city:mCityList){
            i++;
            String cityName=city.getCity();
            String cityCode=city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    public List<City> getmCityList() {
        return mCityList;
    }

    //    把sql放在内存中
    public static MyApplication getInstance(){
        return mApplication;
    }

//    把db中的列表存到android能够读取的数据库中
    private CityDB openCityDB(){
        String path="/data"+ Environment.getDataDirectory().getAbsolutePath()+ File.separator+getPackageName()+File.separator+"databases1"+File.separator+CityDB.CITY_DB_NAME;
        File db=new File(path);
        Log.d(TAG,path);
//        android中db不存在，新建db地址
        if(!db.exists()){
            String pathfolder="/data"+ Environment.getDataDirectory().getAbsolutePath()+ File.separator+getPackageName()+File.separator+"databases1"+File.separator;
            File dirFirstFolder=new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
//            把db内容写入android db中
            try {
                InputStream inputStream = getAssets().open("city.db");
                FileOutputStream fileOutputStream = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    fileOutputStream.flush();
                }
            }
            catch(IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this,path);
    }
}
