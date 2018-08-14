package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.work.WorkBlock;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpBlockInfo;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2016/12/15 14:36.
 * Explain:
 */
public class ResultPanelAllShip1 extends JPanel {

    private int width = GlobalData.reWidth;
    private int height = GlobalData.reHeight - 100; //整个结果图的高度

    private int leftMargin = 80;//左边距
    private int topMargin = 20; //上边距

    private long maxTime = 0, minTime = Long.MAX_VALUE;//船期坐标轴最大时间与最小时间
    private Date stTime;

    private int ratio = 4;
    private int head = 60; //图中船身高度
    private int gap = 20; //船身到时间轴的间隙
    private int cwpBlock = height - topMargin - head; //时间轴，画作业块的高度

    private int m = 3600;

    private WorkingData workingData;
    private StructureData structureData;
    private List<WorkBlock> workBlockList;
    private String type;

    private List<Ship> shipList;//存放船

    public ResultPanelAllShip1(AllRuntimeData allRuntimeData, Long berthId, List<WorkBlock> workBlockList, String type) {
        this.workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        this.structureData = allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode());
        this.workBlockList = workBlockList;
        this.type = type;
        shipList = new ArrayList<>();
        initTime();
        initShip();
        initComponents();
    }

    private void initTime() {
        if (type.equals(GlobalData.PLAN)) {
            long st = workingData.getVmSchedule().getPlanBeginWorkTime().getTime() / 1000;
            long ed = workingData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000;
            if (st < minTime) {
                minTime = st;
                stTime = workingData.getVmSchedule().getPlanBeginWorkTime();
            }
            if (ed > maxTime) {
                maxTime = ed;
            }
            maxTime = maxTime - minTime;//得到相对时间，最小时间就为0
        }
    }

    private void initShip() {
        long st = workingData.getVmSchedule().getPlanBeginWorkTime().getTime() / 1000;
        long ed = workingData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000;
        int t = (int) (ed - st);//船期时间
        int s = (int) (st - minTime);//开始时间与最小时间间隔
        int sp = workingData.getVmSchedule().getPlanStartPst().intValue();
        int ep = workingData.getVmSchedule().getPlanEndPst().intValue();
        int d = ep - sp;
        sp = 10;
        ep = sp + d;
        int shipX = leftMargin + sp;//船头X坐标
        int shipY = (int) ((cwpBlock) * ((double) s / maxTime)) + topMargin;//船头Y坐标
        int shipWidth = ep - sp;//船长度
        Ship ship = new Ship(shipX, shipY, shipWidth * ratio);
        int planHeight = (int) ((cwpBlock) * ((double) (t - 3600) / maxTime)) + head;
        ship.setPlanHeight(planHeight);
        ship.setLR(workingData.getVmSchedule().getPlanBerthDirect());
        ship.setMaxTime(maxTime);
        ship.setMinTime(minTime);
        ship.setTimeBlock(cwpBlock);
        ship.setType(type);
        shipList.add(ship);
    }

    private void initComponents() {
        this.setPreferredSize(new Dimension(width, height + 300));
        this.setSize(width, height + 300);
        this.setOpaque(true);
        this.setLayout(null);
        for (Ship ship : shipList) {
            ShipPanelAllShip1 shipPanel = new ShipPanelAllShip1(ship, structureData, workBlockList);
            this.add(shipPanel);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setPaint(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(leftMargin, topMargin, leftMargin, height);

        g2d.setStroke(new BasicStroke(1));
        g2d.drawString("总时间:", leftMargin - 50, topMargin);
        g2d.drawString(secToTime((int) maxTime), leftMargin - 50, topMargin + 20);
        int timeStep = (int) maxTime % m == 0 ? (int) maxTime / m : (int) maxTime / m + 1;
        for (int j = 0; j <= timeStep; j++) {
            String tStr = secToTime(j * m);
            int timeY = j * (cwpBlock) / timeStep + topMargin + head;
            g2d.drawString(tStr, leftMargin - 50, timeY + 5);
            g2d.drawLine(leftMargin - 5, timeY, leftMargin, timeY);
        }
    }

    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            hour = minute / 60;
            minute = minute % 60;
            timeStr = unitFormat(hour) + ":" + unitFormat(minute);
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        return i >= 0 && i < 10 ? "0" + Integer.toString(i) : "" + i;
    }
}
