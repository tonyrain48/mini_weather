package com.wxydsb.myweather.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.wxydsb.myweather.R;
import com.wxydsb.myweather.app.MyApplication;
import com.wxydsb.myweather.bean.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//城市选择的Activity
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView mList;
    private TextView mText;
    private SearchView mSearch;

    private List<City> cityList;
    private MyApplication myApplication;
    private ArrayList<String> mArrayList;
    private ArrayList<String> mSearchList;
    private ArrayList<String> mCodeList;
    private Map<String,String> toCode=new HashMap<>();
//    默认为北京
    private String updateCitycode="101010100";
    private String updateCityname="北京";

//    初始化
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn=(ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mList=(ListView) findViewById(R.id.title_list);

        mText=(TextView) findViewById(R.id.title_name);

//      读db数据库，放在listview中显示
        myApplication=(MyApplication)getApplication();
        cityList=myApplication.getmCityList();
        mArrayList=new ArrayList<String>();
        mSearchList=new ArrayList<String>();
        mCodeList=new ArrayList<String>();
        toCode=new HashMap<>();
        for(int i=0;i<cityList.size();i++) {
            String cityName = cityList.get(i).getCity();
            mArrayList.add(cityName);
        }
        String strName;
        String strCode;
        for(City cityName:cityList){
            strCode=cityName.getNumber();
            strName=cityName.getCity();
            toCode.put(strName,strCode);
            mSearchList.add(strName);
            mCodeList.add(toCode.get(strName));
        }
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,mSearchList);
        mList.setAdapter(adapter);

        mSearch=(SearchView) findViewById(R.id.title_search);
        mSearch.setIconified(true);
        mSearch.setQueryHint("请输入城市名");

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    if(mSearchList!=null){
                        mSearchList.clear();
                        mCodeList.clear();
                    }
                    for(String str:toCode.keySet()){
                        if(str.contains(newText)){
                            mSearchList.add(str);
                            mCodeList.add(toCode.get(str));
                        }
                    }
                    mList.setAdapter(adapter);
                }
                return true;
            }
        });

//        监听listview的显示，返回城市代码
        AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateCitycode=mCodeList.get(position);
                Log.d("updatecode",updateCitycode);
//                title更新城市名
                updateCityname=mSearchList.get(position);
                mText.setText("当前城市："+updateCityname);
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
