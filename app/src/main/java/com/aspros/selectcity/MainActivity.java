package com.aspros.selectcity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private boolean isFirstInit = false;

    private DatabaseHelper dbHelp;
    private SQLiteDatabase db;


    private List<BaseInfo> provinceList = new ArrayList<BaseInfo>();
    private List<BaseInfo> cityList = new ArrayList<BaseInfo>();
    private List<BaseInfo> areaList = new ArrayList<BaseInfo>();

    private WheelView province_Wheel;
    private WheelView city_Wheel;
    private WheelView area_Wheel;

    private String provinceId = null;
    private String cityId = null;

    private TextView tv_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        dbHelp = new DatabaseHelper(this, "countryList.db", null, 1);
        db = dbHelp.getWritableDatabase();

        tv_address = (TextView) findViewById(R.id.tv_address);

        findViewById(R.id.show_address).setOnClickListener(this);

        init();
    }
    //判断是否加载过数据
    private void init() {
        SharedPreferences sharedPreferences = getSharedPreferences("loadData", MODE_PRIVATE);
        isFirstInit = sharedPreferences.getBoolean("isFirstIn", true);
        if (!isFirstInit) {
        } else {
            Intent intent = new Intent(this, LoadDataService.class);
            startService(intent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstIn", false);
            editor.commit();
        }
    }

    @Override
    public void onClick(View v) {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        // 引入窗口配置文件 - 即弹窗的界面
        final View view = inflater.inflate(R.layout.activity_address_select, null);

        province_Wheel = (WheelView) view.findViewById(R.id.province_Wheel);
        city_Wheel = (WheelView) view.findViewById(R.id.city_Wheel);
        area_Wheel = (WheelView) view.findViewById(R.id.area_Wheel);
        area_Wheel.setVisibility(View.INVISIBLE);
        province_Wheel.setOffset(0);
        province_Wheel.setItems(getProvinceList());
        province_Wheel.setSelection(0);
        province_Wheel.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(BaseInfo p) {
                provinceId = p.getId();
                city_Wheel.setOffset(0);
                city_Wheel.setItems(getCityList(p.getId()));
                city_Wheel.setSelection(0);
                city_Wheel.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(BaseInfo bi) {
                        cityId = bi.getId();

//                        Toast.makeText(Address_Manager.this, provinceId+"|"+cityId, Toast.LENGTH_SHORT).show();
                        if (getAreaList(provinceId, cityId).size() > 0) {
                            area_Wheel.setVisibility(View.VISIBLE);
                            area_Wheel.setOffset(0);
                            area_Wheel.setItems(getAreaList(provinceId, cityId));
                            area_Wheel.setSelection(0);
                        } else {
                            area_Wheel.setVisibility(View.INVISIBLE);
//                            area_Wheel.setOffset(0);
//                            area_Wheel.setItems(null);
//                            area_Wheel.setSelection(0);
                        }
                    }
                });
            }
        });

        // PopupWindow实例化
        final PopupWindow pop = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //设置动画
        pop.setAnimationStyle(R.style.animation);
        pop.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        //监听点击事件
        view.findViewById(R.id.back_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (area_Wheel.getVisibility() != View.INVISIBLE) {
                    tv_address.setText(province_Wheel.getSelectedItem() + "|" + city_Wheel.getSelectedItem() + "|" + area_Wheel.getSelectedItem());
                } else {
                    tv_address.setText(province_Wheel.getSelectedItem() + "|" + city_Wheel.getSelectedItem());
                }
                pop.dismiss();
            }
        });
    }

    //加载城市数据
    private List<BaseInfo> getProvinceList() {
        Cursor cursor = db.rawQuery("select * from province", null);
        while (cursor.moveToNext()) {
            String provinceName = cursor.getString(cursor.getColumnIndex("provinceName"));
            String provinceId = cursor.getString(cursor.getColumnIndex("id"));
            BaseInfo pi = new BaseInfo();
            pi.setId(provinceId);
            pi.setShowName(provinceName);
            provinceList.add(pi);
        }
        cursor.close();
        return provinceList;
    }

    //根据省份id获取城市
    private List<BaseInfo> getCityList(String provinceId) {
        cityList = new ArrayList<BaseInfo>();
        Cursor cursor = db.rawQuery("select * from city where provinceId=?", new String[]{provinceId});
        while (cursor.moveToNext()) {
            String cityName = cursor.getString(cursor.getColumnIndex("cityName"));
            String cityId = cursor.getString(cursor.getColumnIndex("id"));
            BaseInfo ci = new BaseInfo();
            ci.setId(cityId);
            ci.setShowName(cityName);
            cityList.add(ci);
        }
        cursor.close();
        return cityList;
    }

    //根据省份id和城市id获取区县
    private List<BaseInfo> getAreaList(String provinceId, String cityId) {
        areaList = new ArrayList<BaseInfo>();
        if (provinceId != null && cityId != null) {

            Cursor cursor = db.rawQuery("select * from area where provinceId=? and cityId=?", new String[]{provinceId, cityId});
            while (cursor.moveToNext()) {
                String areaName = cursor.getString(cursor.getColumnIndex("areaName"));
                String areaId = cursor.getString(cursor.getColumnIndex("id"));
                BaseInfo ai = new BaseInfo();
                ai.setId(areaId);
                ai.setShowName(areaName);
                areaList.add(ai);
            }
            cursor.close();
        } else {
        }
        return areaList;
    }
}
