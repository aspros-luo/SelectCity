package com.aspros.selectcity;

/**
 * Created by Aspros on 16/4/10.
 */
public class BaseInfo {
    String id;
    String showName;

    public void setShowName(String provinceName) {
        this.showName = provinceName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShowName() {
        return showName;
    }

    public String getId() {
        return id;
    }

    //重写toString方法填充数据到下拉框
    @Override
    public String toString() {
        return showName;
    }
}
