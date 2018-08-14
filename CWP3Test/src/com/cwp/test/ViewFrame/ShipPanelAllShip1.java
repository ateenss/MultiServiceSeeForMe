package com.cwp.test.ViewFrame;


import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.vessel.VMHatch;
import com.cwp3.model.work.WorkBlock;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2016/12/15 15:30.
 * Explain:
 */
public class ShipPanelAllShip1 extends JPanel {

    private int ratio = 4;
    private int head = 60;
    private int bold = 2;
    private int shipHeight = (int) (12 * ratio);
    private int curveHead = 15;
    private int curveTail = 5;
    private int blockWidthD = (int) (10 * ratio);
    private int blockWidthX = (int) (6 * ratio);
    private int timeBlock;

    private String RL;

    private Ship ship;
    private List<WorkBlock> workBlockList;
    private StructureData structureData;

    public ShipPanelAllShip1(Ship ship, StructureData structureData, List<WorkBlock> workBlockList) {
        this.ship = ship;
        this.structureData = structureData;
        this.workBlockList = workBlockList;
        this.timeBlock = ship.getTimeBlock();
        this.setOpaque(false);  //透明
        this.setPreferredSize(new Dimension(ship.getShipWidth() + bold / 2, ship.getPlanHeight() + head + bold / 2 + 300));
        this.setBounds(ship.getShipX(), ship.getShipY(), ship.getShipWidth() + bold / 2, ship.getPlanHeight() + head + bold / 2 + 300);
    }

    private int getXPosition(String rl, int po) {
        if (rl.equals("L")) {
            return po;
        } else {
            return ship.getShipWidth() - po;
        }
    }

    private int getOpposite(String rl, int n) {
        if (rl.equals("L")) {
            return n;
        } else {
            return 0 - n;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);

        g2d.setStroke(new BasicStroke(bold));
        g2d.drawLine(0, 0, ship.getShipWidth(), 0);
        if (ship.getLR().equals("L")) {
            g2d.drawLine(curveHead, shipHeight, ship.getShipWidth() - curveTail, shipHeight);
            QuadCurve2D curveHead = new QuadCurve2D.Double(0, 0, this.curveHead + this.curveHead, shipHeight - 20, this.curveHead, shipHeight);
            g2d.draw(curveHead);
            QuadCurve2D curveTail = new QuadCurve2D.Double(ship.getShipWidth() - this.curveTail, shipHeight, ship.getShipWidth() - this.curveTail - this.curveTail, shipHeight - 20, ship.getShipWidth(), 0);
            g2d.draw(curveTail);
            RL = "L";
        }
        if (ship.getLR().equals("R")) {
            g2d.drawLine(this.curveTail, shipHeight, ship.getShipWidth() - curveHead, shipHeight);
            QuadCurve2D curveLeft = new QuadCurve2D.Double(ship.getShipWidth() - curveHead, shipHeight, ship.getShipWidth() - curveHead - curveHead, shipHeight - 20, ship.getShipWidth(), 0);
            g2d.draw(curveLeft);
            QuadCurve2D curveTail = new QuadCurve2D.Double(0, 0, this.curveTail + this.curveTail, shipHeight - 20, this.curveTail, shipHeight);
            g2d.draw(curveTail);
            RL = "R";
        }

//        g2d.setStroke(new BasicStroke(1));
//        g2d.drawRect(0, 0, ship.getShipWidth(), ship.getPlanHeight() + head);

        //画舱和倍位信息
        Map<Integer, Integer> bayPositionQuery = new HashMap<>();
        g2d.setStroke(new BasicStroke(1));
        List<Long> hatchIds = structureData.getAllHatchIdList();
        for (Long hatchId : hatchIds) {
            VMHatch moHatch = structureData.getVMHatchByHatchId(hatchId);
            //画舱开始坐标线
            g2d.setColor(Color.BLACK);
            int hatchX = moHatch.getHatchPosition().intValue();
            g2d.drawLine(getXPosition(RL, hatchX * ratio), head,
                    getXPosition(RL, hatchX * ratio), ship.getPlanHeight());
            //画舱中心虚线
            g2d.setPaint(Color.LIGHT_GRAY);
            g2d.drawLine(getXPosition(RL, (hatchX + moHatch.getHatchLength().intValue() / 2) * ratio), shipHeight,
                    getXPosition(RL, (hatchX + moHatch.getHatchLength().intValue() / 2) * ratio), shipHeight + ship.getPlanHeight());
            //画舱结束坐标线
            g2d.setColor(Color.BLACK);
            hatchX = hatchX + moHatch.getHatchLength().intValue();
            g2d.drawLine(getXPosition(RL, hatchX * ratio), head,
                    getXPosition(RL, hatchX * ratio), ship.getPlanHeight());
            //画倍位号
            g2d.setPaint(Color.BLACK);
            List<Integer> bayNos = moHatch.getBayNos();
            for (Integer bayNo : bayNos) {
                int bayX = moHatch.getVMBayPosition(bayNo).intValue() - getOpposite(RL, 1);
                g2d.drawString(bayNo + "", getXPosition(RL, bayX * ratio), shipHeight / 4);
                bayPositionQuery.put(bayNo, bayX * ratio);
            }
            if (bayNos.size() == 2) {
                int bayX = (moHatch.getVMBayPosition(bayNos.get(0)).intValue() + moHatch.getVMBayPosition(bayNos.get(1)).intValue()) / 2;
                bayPositionQuery.put((bayNos.get(0) + bayNos.get(1)) / 2, bayX * ratio);
            }
        }

        //设置桥机颜色
        List<String> craneSet = Arrays.asList("101", "102", "103", "104", "105", "106", "107", "108", "110",
                "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123",
                "124", "125", "126", "127", "128", "129", "130");
        Color[] colors = new Color[]{new Color(0xCD00CD), new Color(0x00FFFF), new Color(0xFF0325), new Color(0x9F79EE),
                new Color(0x21FE06), new Color(0xFFFF22), new Color(0xFF00FF), new Color(0x9AA309), new Color(0x120DFF),
                new Color(0x8B0000), new Color(0x1EC6CD), new Color(0x87CEFA), new Color(0xEE0000), new Color(0x000077),
                new Color(0x22B522), new Color(0x3D3D3D), new Color(0x050505)};//16部桥机的颜色
        Map<String, Color> craneQuery = new HashMap<>();
        int k = 0;
        for (String craneNo : craneSet) {
            if (k > 14) {
                k = 0;
            }
            craneQuery.put(craneNo, colors[k++]);
        }

        //画作业块信息
        Map<Integer, Long> countQuery = new HashMap<>();//每个倍位的moveCount数统计
        for (WorkBlock smartReCwpBlockInfo : workBlockList) {
            String craneNo = smartReCwpBlockInfo.getCraneNo();//得到桥机号
            int bayNo = Integer.valueOf(smartReCwpBlockInfo.getBayNo());//得到倍位号
            Long startTime = smartReCwpBlockInfo.getWorkingStartTime().getTime() / 1000 - ship.getMinTime();
            Long endTime = smartReCwpBlockInfo.getWorkingEndTime().getTime() / 1000 - ship.getMinTime();
            Long moveCount = smartReCwpBlockInfo.getPlanAmount();
            Long craneSeq = smartReCwpBlockInfo.getCraneSeq();
            Long hatchSeq = smartReCwpBlockInfo.getHatchSeq();
            String ldFlag = smartReCwpBlockInfo.getLduldfg();
            if (countQuery.get(bayNo) != null) {
                countQuery.put(bayNo, countQuery.get(bayNo) + moveCount);
            } else {
                countQuery.put(bayNo, moveCount);
            }
            int w = bayNo % 2 == 0 ? blockWidthD : blockWidthX;//作业块宽度
            int x = getXPosition(RL, bayPositionQuery.get(bayNo)) - w / 2 + 3;
            int y = head + startTime.intValue() * (timeBlock) / ship.getMaxTime().intValue();
            int h = (endTime.intValue() - startTime.intValue()) * (timeBlock) / ship.getMaxTime().intValue();//作业块长度
            g2d.setPaint(craneQuery.get(craneNo));
            g2d.drawRect(x, y, w, h);
            g2d.fillRect(x, y, w, h);

            //画块上面的moveCount数
            g2d.setPaint(Color.BLACK);
            int mx = x + w / 4;
            int my = y + h / 2;
            g2d.drawString(String.valueOf(moveCount), mx, my);

            //画装卸标志
            g2d.setPaint(Color.BLACK);
            int mx1 = x + 3 * w / 4;
            g2d.drawString(ldFlag, mx1, my);

            //画舱和桥机的顺序
//            g2d.drawString(String.valueOf(craneSeq), x + 18, y + 10);
//            g2d.drawString(String.valueOf(hatchSeq), x + 8, y + 10);
        }

        //遍历Map，画出每个倍位的moveCount数
        if (countQuery != null) {
            for (Map.Entry<Integer, Long> entry : countQuery.entrySet()) {
                int x = getXPosition(RL, bayPositionQuery.get(Integer.valueOf(entry.getKey())));
                g2d.setPaint(Color.red);
                if (Integer.valueOf(entry.getKey()) % 2 == 0) {
                    g2d.drawString(entry.getValue() + "", x - 2, 28);
                } else {
                    g2d.drawString(entry.getValue() + "", x + 2, 40);
                }
            }
        }
    }

}
