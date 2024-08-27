package com.example.project;
public class NowGas {
    //주로 주유소와 관련된 정보를 저장하고 관리하는 용도로 사용
    double x;
    double y;
    //주유소 x, y 좌표를 저장
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    int price;
    //주유소에서 제공하는 연료 가격 저장
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    String OS;
    //주유소의 브랜드 또는 이름을 저장하는 문자열
    public String getOS() { return OS; }

    public void setOS(String OS) {
        this.OS = OS;
    }

    float distance;
    //특정 지점에서 해당 주유소까지의 거리 저장
    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
