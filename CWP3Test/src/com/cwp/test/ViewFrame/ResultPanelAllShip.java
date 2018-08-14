package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.model.vessel.VMSchedule;
import com.cwp3.model.work.WorkBlock;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpBlockInfo;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.*;
import java.util.List;

/**
 * Created by csw on 2016/12/15 14:36.
 * Explain:
 */
public class ResultPanelAllShip extends JPanel {

    private AllRuntimeData allRuntimeData;
    private String type;
    private SmartCwpImportData smartCwpImportData;

    private int width = GlobalData.reWidth;
    private int height = GlobalData.reHeight - 100; //整个结果图的高度

    private int leftMargin = 80;//左边距
    private int topMargin = 20; //上边距

    private long maxTime = 0, minTime = Long.MAX_VALUE;//船期坐标轴最大时间与最小时间
    private Date stTime;

    private int ratio = 4;
    private int head = 100; //图中船身高度
    private int gap = 20; //船身到时间轴的间隙
    private int cwpBlock = height - topMargin - head; //时间轴，画作业块的高度

    private int m = 3600;
    private List<Ship> shipList;//存放船

    public ResultPanelAllShip(SmartCwpImportData smartCwpImportData, AllRuntimeData allRuntimeData, String type) {
        this.allRuntimeData = allRuntimeData;
        this.type = type;
        this.smartCwpImportData = smartCwpImportData;
        shipList = new ArrayList<>();
        TimePanel timePanel = new TimePanel();
        timePanel.setPreferredSize(new Dimension(100,0));
        this.setLayout(new BorderLayout());
        this.add(timePanel,BorderLayout.WEST);
        initShip();
        initComponents();
    }

    private void initComponents() {
        this.setOpaque(true);
        for (Ship ship:shipList) {
            ShipPanelAllShip shipPanel = new ShipPanelAllShip(smartCwpImportData,ship);
            this.add(shipPanel,BorderLayout.CENTER);
        }
    }

    class TimePanel extends JPanel{
        public TimePanel(){
            if (type.equals(GlobalData.PLAN)) {
                List<VMSchedule> allScheduleList = new ArrayList<>();
                for(Long berthId:allRuntimeData.getAllBerthId()){
                    allScheduleList.add(allRuntimeData.getWorkingDataByBerthId(berthId).getVmSchedule());
                }
                for (VMSchedule cwpSchedule : allScheduleList) {
                    long st = cwpSchedule.getPlanBeginWorkTime().getTime() / 1000;
                    long ed = cwpSchedule.getPlanEndWorkTime().getTime() / 1000;
                    if (st < minTime) {
                        minTime = st;
                        stTime = cwpSchedule.getPlanBeginWorkTime();
                    }
                    if (ed > maxTime) {
                        maxTime = ed;
                    }
                }
                maxTime = maxTime - minTime;//得到相对时间，最小时间就为0
            }
            if (type.equals(GlobalData.REWORK)) {
                Map<String, List<WorkBlock>> allWorkBlockMap = new HashMap<String, List<WorkBlock>>();
                Integer i = 0;
                for (Long berthId : allRuntimeData.getAllBerthId()) {
                    for(List<WorkBlock> workBlockList:allRuntimeData.getWorkingDataByBerthId(berthId).getWorkBlockMap().values()){
                        allWorkBlockMap.put(i.toString(),workBlockList);
                        i++;
                    }
                }
                for (List<WorkBlock> workBlocks: allWorkBlockMap.values()) {
                    for (WorkBlock workBlock : workBlocks) {
                        long st = workBlock.getWorkingStartTime().getTime() / 1000;
                        long ed = workBlock.getWorkingEndTime().getTime() / 1000;
                        if (st < minTime) {
                            minTime = st;
                            stTime = workBlock.getWorkingStartTime();
                        }
                        if (ed > maxTime) {
                            maxTime = ed;
                        }
                    }
                }
                maxTime = maxTime - minTime;//得到相对时间，最小时间就为0
            }
        }

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
    }

    private void initShip() {
        List<VMSchedule> allScheduleList = new ArrayList<>();
        for(Long berthId:allRuntimeData.getAllBerthId()){
            allScheduleList.add(allRuntimeData.getWorkingDataByBerthId(berthId).getVmSchedule());
        }
        for (VMSchedule cwpSchedule : allScheduleList) {

            long st = cwpSchedule.getPlanBeginWorkTime().getTime() / 1000;
            long ed = cwpSchedule.getPlanEndWorkTime().getTime() / 1000;
            int t = (int) (ed - st);//船期时间
            int s = (int) (st - minTime);//开始时间与最小时间间隔
            if (type.equals(GlobalData.REWORK)) {
                //计算该船作业块最小开始时间、最大结束时间
                long maxT = Long.MIN_VALUE;
                long minT = Long.MAX_VALUE;
                Map<String, List<WorkBlock>> allWorkBlockMap = new HashMap<String, List<WorkBlock>>();
                Integer i = 0;
                for (Long berthId : allRuntimeData.getAllBerthId()) {
                    for(List<WorkBlock> workBlockList:allRuntimeData.getWorkingDataByBerthId(berthId).getWorkBlockMap().values()){
                        allWorkBlockMap.put(i.toString(),workBlockList);
                        i++;
                    }
                }
                for (List<WorkBlock> workBlocks: allWorkBlockMap.values()) {
                    for (WorkBlock workBlock : workBlocks) {
                        if (workBlock.getWorkingStartTime().getTime() < minT) {
                            minT = workBlock.getWorkingStartTime().getTime();
                        }
                        if (workBlock.getWorkingEndTime().getTime() > maxT) {
                            maxT = workBlock.getWorkingEndTime().getTime();
                        }
                    }
                }
                st = minT / 1000;
                ed = maxT / 1000;
                t = (int) (ed - st);//船期时间
                s = (int) (st - minTime);//开始时间与最小时间间隔
            }
            int sp = cwpSchedule.getPlanStartPst().intValue();
            int ep = cwpSchedule.getPlanEndPst().intValue();
            int shipX = leftMargin + sp;//船头X坐标
            int shipY = (int) ((cwpBlock) * ((double) s / maxTime)) + topMargin;//船头Y坐标
            int shipWidth = ep - sp;//船长度
            Ship ship = new Ship(shipX, shipY, shipWidth * ratio);
            int planHeight = (int) ((cwpBlock) * ((double) t / maxTime)) + head;
            ship.setPlanHeight(planHeight);
            ship.setLR(cwpSchedule.getPlanBerthDirect());
            ship.setMaxTime(maxTime);
            ship.setMinTime(minTime);
            ship.setTimeBlock(cwpBlock);
            ship.setAllRuntimeData(allRuntimeData);
            ship.setBerthId(cwpSchedule.getBerthId());
            ship.setType(type);
            shipList.add(ship);
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
