package com.wxydsb.myweather.bean;

public class TodayWeather {
    private String city;
    private String updatetime;
    private String wendu;
    private String shidu;
    private String pm25;
    private String quality;
    private String fengxiang;
    private String fengli;
    private String date;
    private String high;
    private String low;
    private String type;

    public String getCity() {
        return city;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public String getPm25() {
        return pm25;
    }

    public String getWendu() {
        return wendu;
    }

    public String getQuality() {
        return quality;
    }

    public String getShidu() {
        return shidu;
    }

    public String getDate() {
        return date;
    }

    public String getFengli() {
        return fengli;
    }

    public String getFengxiang() {
        return fengxiang;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getType() {
        return type;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public void setShidu(String shidu) {
        this.shidu = shidu;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public void setFengxiang(String fengxiang) {
        this.fengxiang = fengxiang;
    }

    public void setWendu(String wendu) {
        this.wendu = wendu;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setFengli(String fengli) {
        this.fengli = fengli;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString(){
        return "TodayWeather{"+
                "city='"+city+'\''+
                ", updatetime='"+updatetime+'\''+
                ", wendu='"+wendu+'\''+
                ", shidu='"+shidu+'\''+
                ", pm25='"+pm25+'\''+
                ", quality='"+quality+'\''+
                ", fengxiang='"+fengxiang+'\''+
                ", fengli='"+fengli+'\''+
                ", date='"+date+'\''+
                ", high='"+high+'\''+
                ", low='"+low+'\''+
                ", type='"+type+'\''+
                '}';
    }
}
