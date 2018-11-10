package com.wxydsb.myweather.miniweather;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.wxydsb.myweather.R;
import com.wxydsb.myweather.bean.TodayWeather;
import com.wxydsb.myweather.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//主页面
public class MainActivity extends Activity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER=1,DB=2;

    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    private ImageView mLocation;

    private ViewPager mViewPager;

    private List<View> views;

    private String type;

    private TextView cityTv,timeTv,humidtyTv,weekTv,pmDataTv,pmQualityTv,temperatureTv,temperatuer_cur_Tv,climateTv,windTv,city_name_Tv;
    private TextView dayone_dateTv,dayone_tempTv,dayone_typeTv,dayone_fengliTv,daytwo_dateTv,daytwo_tempTv,daytwo_typeTv,daytwo_fengliTv,daythree_dateTv,daythree_tempTv,daythree_typeTv,daythree_fengliTv,dayfour_dateTv,dayfour_tempTv,dayfour_typeTv,dayfour_fengliTv;
    private ImageView weatherImg,pmImg;
    private ImageView dayone_Img,daytwo_Img,daythree_Img,dayfour_Img;

    private MyPagerAdapter myPagerAdapter;

    public LocationClient mLocationClient=null;
    private MyLocationListener myLocationListener=new MyLocationListener();

//    传递消息
    private Handler mHandler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
//                通过msg信息更新天气
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

//    初始化界面
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        永久保存
        SharedPreferences sharedPreferencesmain=getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferencesmain.edit();
        String cityCodemain=sharedPreferencesmain.getString("main_city_code",null);
        if(cityCodemain==null){
            editor.clear();
            editor.putString("main_city_code","101010100");
            editor.commit();

        }

        mUpdateBtn=(ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mLocation=(ImageView) findViewById(R.id.title_location);
        mLocation.setOnClickListener(this);

        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListener);
        initLocation();
        mLocationClient.start();

//        网络状态检测
        if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
            Log.d("myweather","网络已连接");
            Toast.makeText(MainActivity.this,"网络已连接", Toast.LENGTH_LONG).show();
            queryWeatherCode(cityCodemain);
        }
        else{
            Log.d("myweather","网络未连接");
            Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
        }

        mCitySelect=(ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initViewPager();
        initView();
    }

//    初始化视图
    void initView(){
        View view1=LayoutInflater.from(this).inflate(R.layout.viewpager1,null);
        View view2=LayoutInflater.from(this).inflate(R.layout.viewpager2,null);
        city_name_Tv=(TextView) findViewById(R.id.title_city_name);
        cityTv=(TextView) findViewById(R.id.city);
        timeTv=(TextView) findViewById(R.id.time);
        humidtyTv=(TextView) findViewById(R.id.humidity);
        weekTv=(TextView) findViewById(R.id.week_today);
        pmDataTv=(TextView) findViewById(R.id.pm_data);
        pmQualityTv=(TextView) findViewById(R.id.pm2_5_quality);
        pmImg=(ImageView) findViewById(R.id.pm2_5_image);
        temperatureTv=(TextView) findViewById(R.id.temperature);
        temperatuer_cur_Tv=(TextView) findViewById(R.id.temperature_cur);
        climateTv=(TextView) findViewById(R.id.climate);
        windTv=(TextView) findViewById(R.id.wind);
        weatherImg=(ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidtyTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        temperatuer_cur_Tv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

//    通过网站抓取天气信息
    private void queryWeatherCode(final String cityCode){
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        Log.d("myweather",address);
//        用线程去后台读取数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather=null;
                try{
                    URL url=new URL(address);
                    con=(HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in=con.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));  //缓冲区
                    StringBuilder response=new StringBuilder();
                    String str;
                    while((str=reader.readLine())!=null){
                        response.append(str);
                        Log.d("myweather",str);
                    }
                    String responseStr=response.toString();
                    Log.d("myweather",responseStr);
                    todayWeather=parseXML(responseStr);
//                    把天气信息传回主界面用于更新
                    if(todayWeather!=null){
                        Log.d("myweather",todayWeather.toString());
                        Message msg=new Message();
                        msg.what=UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    if(con!=null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

//    XML解析
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount=0;
        int fengliCount=0;
        int dateCount=0;
        int highCount=0;
        int lowCount=0;
        int typeCount=0;
        try{
            XmlPullParserFactory fac=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType=xmlPullParser.getEventType();
            Log.d("myweather","parseXML");
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather=new TodayWeather();
                        }
                        if(todayWeather!=null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType=xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("updatetime")){
                                eventType=xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("shidu")){
                                eventType=xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("wendu")){
                                eventType=xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("pm25")){
                                eventType=xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("quality")){
                                eventType=xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            else if(xmlPullParser.getName().equals("fengli") && fengliCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }
                            else if(xmlPullParser.getName().equals("date") && dateCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if(xmlPullParser.getName().equals("high") && highCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if(xmlPullParser.getName().equals("type") && typeCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if(xmlPullParser.getName().equals("date") && dateCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayone_date(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if(xmlPullParser.getName().equals("high") && highCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayone_high(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayone_low(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if(xmlPullParser.getName().equals("type") && typeCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayone_type(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if(xmlPullParser.getName().equals("fengli") && fengliCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayone_fengli(xmlPullParser.getText());
                                fengliCount++;
                            }
                            else if(xmlPullParser.getName().equals("date") && dateCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaytwo_date(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if(xmlPullParser.getName().equals("high") && highCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaytwo_high(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaytwo_low(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if(xmlPullParser.getName().equals("type") && typeCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaytwo_type(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if(xmlPullParser.getName().equals("fengli") && fengliCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaytwo_fengli(xmlPullParser.getText());
                                fengliCount++;
                            }
                            else if(xmlPullParser.getName().equals("date") && dateCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaythree_date(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if(xmlPullParser.getName().equals("high") && highCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaythree_high(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaythree_low(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if(xmlPullParser.getName().equals("type") && typeCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaythree_type(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if(xmlPullParser.getName().equals("fengli") && fengliCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setDaythree_fengli(xmlPullParser.getText());
                                fengliCount++;
                            }
                            else if(xmlPullParser.getName().equals("date") && dateCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayfour_date(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if(xmlPullParser.getName().equals("high") && highCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayfour_high(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayfour_low(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if(xmlPullParser.getName().equals("type") && typeCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayfour_type(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if(xmlPullParser.getName().equals("fengli") && fengliCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setDayfour_fengli(xmlPullParser.getText());
                                fengliCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }
        catch (XmlPullParserException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

//    天气更新，控件操作
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidtyTv.setText("湿度："+todayWeather.getShidu());
        temperatuer_cur_Tv.setText("温度："+todayWeather.getWendu()+"℃");
        if(todayWeather.getPm25()==null){
            pmDataTv.setText("无数据");
        }
        else{
            pmDataTv.setText(todayWeather.getPm25());
        }
        if(todayWeather.getQuality()==null){
            pmDataTv.setText("无数据");
        }
        else{
            pmQualityTv.setText(todayWeather.getQuality());
        }
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+'~'+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力："+todayWeather.getFengli());
        pmImg.setImageDrawable(setpmImg(todayWeather));
        weatherImg.setImageDrawable(setweatherImg(todayWeather.getType()));
        dayone_dateTv.setText(todayWeather.getDayone_date());
        dayone_Img.setImageDrawable(setweatherImg(todayWeather.getDayone_type()));
        dayone_fengliTv.setText(todayWeather.getDayone_fengli());
        dayone_typeTv.setText(todayWeather.getDayone_type());
        dayone_tempTv.setText(todayWeather.getDayone_high()+'~'+todayWeather.getDayone_low());
        daytwo_dateTv.setText(todayWeather.getDaytwo_date());
        daytwo_Img.setImageDrawable(setweatherImg(todayWeather.getDaytwo_type()));
        daytwo_fengliTv.setText(todayWeather.getDaytwo_fengli());
        daytwo_typeTv.setText(todayWeather.getDaytwo_type());
        daytwo_tempTv.setText(todayWeather.getDaytwo_high()+'~'+todayWeather.getDaytwo_low());
        daythree_dateTv.setText(todayWeather.getDaythree_date());
        daythree_Img.setImageDrawable(setweatherImg(todayWeather.getDaythree_type()));
        daythree_fengliTv.setText(todayWeather.getDaythree_fengli());
        daythree_typeTv.setText(todayWeather.getDaythree_type());
        daythree_tempTv.setText(todayWeather.getDaythree_high()+'~'+todayWeather.getDaytwo_low());
        dayfour_dateTv.setText(todayWeather.getDayfour_date());
        dayfour_Img.setImageDrawable(setweatherImg(todayWeather.getDayfour_type()));
        dayfour_fengliTv.setText(todayWeather.getDayfour_fengli());
        dayfour_typeTv.setText(todayWeather.getDayfour_type());
        dayfour_tempTv.setText(todayWeather.getDayfour_high()+'~'+todayWeather.getDayfour_low());
        Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
    }

//    修改天气图片
    private Drawable setweatherImg(String type){
        Drawable typedrawable;
        switch (type) {
            case "暴雪":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu);
                break;
            default:
                typedrawable = getResources().getDrawable(R.drawable.biz_plugin_weather_qing);
        }
        return typedrawable;
    }

//    修改PM图片
    private Drawable setpmImg(TodayWeather todayWeather){
        int pm;
        if(todayWeather.getPm25()==null){
            pm=0;
        }
        else
            pm=Integer.parseInt(todayWeather.getPm25());
        Drawable pmdrawable;
        switch (pm){
            case 50:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_0_50);
                break;
            case 100:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_51_100);
                break;
            case 150:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_101_150);
                break;
            case 200:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_151_200);
                break;
            case 300:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_201_300);
                break;
            case 999:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_greater_300);
                break;
            default:
                pmdrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_0_50);
        }
        return pmdrawable;
    }

//    点击事件
    @Override
    public void onClick(View view){
//        选择城市
        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            startActivityForResult(i,1);
        }
//        更新
        if (view.getId()==R.id.title_update_btn){
            SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
            String cityCode=sharedPreferences.getString("main_city_code","101010100");
            Log.d("myweather",cityCode);
            Log.d("click","true");

            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myweather","网络已连接");
                queryWeatherCode(cityCode);
            }
            else{
                Log.d("myweather","网络未连接");
                Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
            }
        }
        if(view.getId()==R.id.title_location){
            String cityCode_location=myLocationListener.cityCode;
            Log.d("click","true");
            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myweather","网络已连接");
                queryWeatherCode(cityCode_location);
            }
            else{
                Log.d("myweather","网络未连接");
                Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
            }
        }
    }

//    选择城市后返回citycode值，更新主界面
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==1&&resultCode==RESULT_OK){
            String newCityCode=data.getStringExtra("cityCode");
            SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("main_city_code",newCityCode);
            editor.commit();

            Log.d("myweather","选择的城市代码为"+newCityCode);

            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myweather","网络已连接");
                queryWeatherCode(newCityCode);
            }
            else{
                Log.d("myweather","网络未连接");
                Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        int span=1000;
        option.setScanSpan(0);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }

    private void initViewPager(){
        mViewPager=(ViewPager) findViewById(R.id.viewpager);
        LayoutInflater lf=LayoutInflater.from(this);
        views=new ArrayList<View>();
        views.add(lf.inflate(R.layout.viewpager1,null));
        views.add(lf.inflate(R.layout.viewpager2,null));
        dayone_dateTv=(TextView) views.get(0).findViewById(R.id.week_dayone);
        dayone_tempTv=(TextView) views.get(0).findViewById(R.id.temp_dayone);
        dayone_typeTv=(TextView) views.get(0).findViewById(R.id.climate_dayone);
        dayone_fengliTv=(TextView) views.get(0).findViewById(R.id.wind_dayone);
        dayone_Img=(ImageView) views.get(0).findViewById(R.id.pic_dayone);
        daytwo_dateTv=(TextView) views.get(0).findViewById(R.id.week_daytwo);
        daytwo_tempTv=(TextView) views.get(0).findViewById(R.id.temp_daytwo);
        daytwo_typeTv=(TextView) views.get(0).findViewById(R.id.climate_daytwo);
        daytwo_fengliTv=(TextView) views.get(0).findViewById(R.id.wind_daytwo);
        daytwo_Img=(ImageView) views.get(0).findViewById(R.id.pic_daytwo);
        daythree_dateTv=(TextView) views.get(1).findViewById(R.id.week_daythree);
        daythree_tempTv=(TextView) views.get(1).findViewById(R.id.temp_daythree);
        daythree_typeTv=(TextView) views.get(1).findViewById(R.id.climate_daythree);
        daythree_fengliTv=(TextView) views.get(1).findViewById(R.id.wind_daythree);
        daythree_Img=(ImageView) views.get(1).findViewById(R.id.pic_daythree);
        dayfour_dateTv=(TextView) views.get(1).findViewById(R.id.week_dayfour);
        dayfour_tempTv=(TextView) views.get(1).findViewById(R.id.temp_dayfour);
        dayfour_typeTv=(TextView) views.get(1).findViewById(R.id.climate_dayfour);
        dayfour_fengliTv=(TextView) views.get(1).findViewById(R.id.wind_dayfour);
        dayfour_Img=(ImageView) views.get(1).findViewById(R.id.pic_dayfour);
        dayone_dateTv.setText("N/A");
        dayone_tempTv.setText("N/A");
        dayone_typeTv.setText("N/A");
        dayone_fengliTv.setText("N/A");
        daytwo_dateTv.setText("N/A");
        daytwo_tempTv.setText("N/A");
        daytwo_typeTv.setText("N/A");
        daytwo_fengliTv.setText("N/A");
        daythree_dateTv.setText("N/A");
        daythree_tempTv.setText("N/A");
        daythree_typeTv.setText("N/A");
        daythree_fengliTv.setText("N/A");
        dayfour_dateTv.setText("N/A");
        dayfour_tempTv.setText("N/A");
        dayfour_typeTv.setText("N/A");
        dayfour_fengliTv.setText("N/A");
        myPagerAdapter=new MyPagerAdapter(views);
        mViewPager.setAdapter(myPagerAdapter);
    }
}
