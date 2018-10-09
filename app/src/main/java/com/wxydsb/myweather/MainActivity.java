package com.wxydsb.myweather;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends Activity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER=1;

    private ImageView mUpdateBtn;

    private TextView cityTv,timeTv,humidtyTv,weekTv,pmDataTv,pmQualityTv,temperatureTv,temperatuer_cur_Tv,climateTv,windTv,city_name_Tv;
    private ImageView weatherImg,pmImg;

    private Handler mHandler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences sharedPreferencesmain=getSharedPreferences("config",MODE_PRIVATE);
        String cityCodemain=sharedPreferencesmain.getString("main_city_code","101010100");

        mUpdateBtn=(ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
            Log.d("myweather","网络已连接");
            Toast.makeText(MainActivity.this,"网络已连接", Toast.LENGTH_LONG).show();
            queryWeatherCode(cityCodemain);
        }
        else{
            Log.d("myweather","网络未连接");
            Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
        }
        initView();
    }

    void initView(){
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

    private void queryWeatherCode(String cityCode){
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        Log.d("myweather",address);
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
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String str;
                    while((str=reader.readLine())!=null){
                        response.append(str);
                        Log.d("myweather",str);
                    }
                    String responseStr=response.toString();
                    Log.d("myweather",responseStr);
                    todayWeather=parseXML(responseStr);
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

    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidtyTv.setText("湿度："+todayWeather.getShidu());
        temperatuer_cur_Tv.setText("温度："+todayWeather.getWendu()+"℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+'~'+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力："+todayWeather.getFengli());
        pmImg.setImageDrawable(setpmImg(todayWeather));
        weatherImg.setImageDrawable(setweatherImg(todayWeather));
        Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
    }

    private Drawable setweatherImg(TodayWeather todayWeather){
        String type=todayWeather.getType();
        Drawable typedrawable;
        switch(type){
            case "暴雪":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_yin);
                break;
            case "雨夹雪":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu);
                break;
            default:
                typedrawable=getResources().getDrawable(R.drawable.biz_plugin_weather_qing);
        }
        return typedrawable;
    }

    private Drawable setpmImg(TodayWeather todayWeather){
        int pm=Integer.parseInt(todayWeather.getPm25());
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
    @Override
    public void onClick(View view){
        if (view.getId()==R.id.title_update_btn){
            SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
            String cityCode=sharedPreferences.getString("main_city_code","101010100");
            Log.d("myweather",cityCode);

            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myweather","网络已连接");
                queryWeatherCode(cityCode);
            }
            else{
                Log.d("myweather","网络未连接");
                Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
            }
        }
    }
}
