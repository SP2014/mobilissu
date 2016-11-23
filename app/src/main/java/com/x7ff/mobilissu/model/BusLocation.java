package com.x7ff.mobilissu.model;


import com.google.gson.annotations.SerializedName;

public class BusLocation {

    @SerializedName("lCode")
    private String lineCode;

    @SerializedName("direction")
    private Integer direction;

    @SerializedName("x")
    private Double x;

    @SerializedName("y")
    private Double y;

    @SerializedName("bearing")
    private Integer bearing;

    @SerializedName("prevStop")
    private String previousStop;

    @SerializedName("currStop")
    private String currentStop;

    @SerializedName("journeyId")
    private String journeyId;

    @SerializedName("deptTime")
    private String departureTime;

    @SerializedName("busIconUrl")
    private Object busIconUrl;

    public String getLineCode() {
        return lineCode;
    }

    public Integer getDirection() {
        return direction;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Integer getBearing() {
        return bearing;
    }

    public String getPreviousStop() {
        return previousStop;
    }

    public String getCurrentStop() {
        return currentStop;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public Object getBusIconUrl() {
        return busIconUrl;
    }
}
