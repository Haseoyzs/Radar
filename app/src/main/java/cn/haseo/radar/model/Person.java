package cn.haseo.radar.model;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

/**
 * 用于存放人物各项数据的类
 */
public class Person extends LitePalSupport {
    // 定义人物名字
    private String name;
    // 定义人物号码
    private String number;
    // 定义人物所在的维度
    private double latitude;
    // 定义人物所在的经度
    private double longitude;
    // 定义人物所在的海拔
    private double altitude;
    // 定义人物的定位精度
    private double accuracy;
    // 定义人物所在的地址
    private String address;
    // 定义人物数据最后的更新时间
    private Date lastUpdated;
    // 用于标记该人物是否是好友
    private boolean isFriend;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isFriend() {
        return isFriend;
    }
    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
