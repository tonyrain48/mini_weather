package com.wxydsb.myweather.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wxydsb.myweather.R;
import com.wxydsb.myweather.app.MyApplication;
import com.wxydsb.myweather.bean.City;

import java.util.ArrayList;
import java.util.List;

//城市选择的Activity
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView mList;

    private List<City> cityList;
    private MyApplication myApplication;
    private ArrayList<String> mArrayList;
//    默认为北京
    private String updateCitycode="101010100";

//    初始化
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn=(ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mList=(ListView) findViewById(R.id.title_list);

//      读db数据库，放在listview中显示
        myApplication=(MyApplication)getApplication();
        cityList=myApplication.getmCityList();
        mArrayList=new ArrayList<String>();
        for(int i=0;i<cityList.size();i++) {
            String cityName = cityList.get(i).getCity();
            mArrayList.add(cityName);
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,mArrayList);
        mList.setAdapter(adapter);

//        监听listview的显示，返回城市代码
        AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateCitycode=cityList.get(position).getNumber();
                Log.d("updatecode",updateCitycode);
            }
        };
        mList.setOnItemClickListener(itemClickListener);
    }


//    listhview的点击
    @Override
    public void onClick(View view){
        switch (view.getId()) {
//            点击返回，此时updatecitycode已更新，通过intent返回给mainActivity
            case R.id.title_back:
                Intent i=new Intent();
                i.putExtra("cityCode",updateCitycode);
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
}
