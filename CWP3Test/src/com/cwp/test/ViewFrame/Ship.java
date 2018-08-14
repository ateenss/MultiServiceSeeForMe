package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;

/**
 * Created by csw on 2016/12/16 11:13.
 * Explain:
 */
public class Ship {

    private int shipX;//船头X坐标
    private int shipY;//船头Y坐标
    private int shipWidth;//船长度
    private int planHeight;//船期时间块高度
    private String LR;//正向、反向
    private Long maxTime;
    private Long minTime;
    private Integer timeBlock; //画作业块的时间轴长度，时间刻度长度
    private AllRuntimeData allRuntimeData;
    private Long berthId;
    private String type;

    public Ship(int shipX, int shipY, int shipWidth) {
        this.shipX = shipX;
        this.shipY = shipY;
        this.shipWidth = shipWidth;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AllRuntimeData getAllRuntimeData() {
        return allRuntimeData;
    }

    public void setAllRuntimeData(AllRuntimeData allRuntimeData) {
        this.allRuntimeData = allRuntimeData;
    }

    public Integer getTimeBlock() {
        return timeBlock;
    }

    public void setTimeBlock(Integer timeBlock) {
        this.timeBlock = timeBlock;
    }

    public Long getMinTime() {
        return minTime;
    }

    public void setMinTime(Long minTime) {
        this.minTime = minTime;
    }

    public Long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Long maxTime) {
        this.maxTime = maxTime;
    }

    public int getShipX() {
        return shipX;
    }

    public void setShipX(int shipX) {
        this.shipX = shipX;
    }

    public int getShipY() {
        return shipY;
    }

    public void setShipY(int shipY) {
        this.shipY = shipY;
    }

    public int getShipWidth() {
        return shipWidth;
    }

    public void setShipWidth(int shipWidth) {
        this.shipWidth = shipWidth;
    }

    public int getPlanHeight() {
        return planHeight;
    }

    public void setPlanHeight(int planHeight) {
        this.planHeight = planHeight;
    }

    public String getLR() {
        return LR;
    }

    public void setLR(String LR) {
        this.LR = LR;
    }
}
