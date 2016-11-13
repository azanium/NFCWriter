package com.azacode.NFCWriter.data;

/**
 * Created by Syuaibi on 9/28/16.
 */

public class NFCData {
    private String securityName;
    private String clientName;
    private String location;
    private String building;
    private String floor;

    public NFCData() {}

    public NFCData(String securityName, String clientName, String location, String building, String floor) {
        this.securityName = securityName;
        this.clientName = clientName;
        this.location = location;
        this.building = building;
        this.floor = floor;
    }

    public String getSecurityName() {
        return securityName;
    }

    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    @Override
    public String toString() {
        return "NFCData{" +
                "securityName='" + securityName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", location='" + location + '\'' +
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                '}';
    }
}
