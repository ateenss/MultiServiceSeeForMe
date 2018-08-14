package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMHatch;
import com.cwp3.model.vessel.VMSchedule;
import com.cwp3.model.work.WorkBlock;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpBlockInfo;
import com.shbtos.biz.smart.cwp.pojo.SmartVpsVslHatchsInfo;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

/**
 * Created by csw on 2016/12/15 15:30.
 * Explain:以舱坐标线为基准，画舱button的宽度
 */
public class ShipPanelAllShip extends JPanel {
    private StructureData structureData;
    private WorkingData workingData;
    private VMSchedule schedule;
    private Map<String, List<WorkBlock>> workBlockMap;
    private List<VMHatch> hatchList;

    private int ratio = 8;
    private int head = 20;
    private int bold = 2;
    private int shipHeight = (int) (12 * ratio);
    private final int curveHead = 15;
    private final int curveTail = 5;
    private int blockWidthD = (int) (12 * ratio);
    private int blockWidthX = (int) (6* ratio);
    private int timeBlock;
    private int machineNum;
    private int shipWidth;
    private Double hatchLength = 0.0;//类中全局变量

    private String RL;

    private Ship ship;

    private SmartCwpImportData smartCwpImportData;
    private List<SmartVpsVslHatchsInfo> smartVpsVslHatchsInfoList;

    public ShipPanelAllShip(SmartCwpImportData smartCwpImportData,Ship ship){
        this.ship = ship;
        this.smartCwpImportData = smartCwpImportData;
        this.smartVpsVslHatchsInfoList = smartCwpImportData.getSmartVpsVslHatchsInfoList();
        this.workingData = ship.getAllRuntimeData().getWorkingDataByBerthId(ship.getBerthId());
        this.schedule = this.workingData.getVmSchedule();
        this.structureData = ship.getAllRuntimeData().getStructDataByVesselCode(schedule.getVesselCode());
        this.workBlockMap = workingData.getWorkBlockMap();
        hatchList = new LinkedList<>(structureData.getAllVMHatchs());
        this.timeBlock = ship.getTimeBlock();
        this.machineNum = calculateMachineNumber(hatchList);
        createTabPanel(ship.getAllRuntimeData(), ship.getBerthId());
    }

    private int calculateMachineNumber(List<VMHatch> hatchList) {
        return 0;
    }

    public void createTabPanel(AllRuntimeData allRuntimeData, Long berthId){
        JTabbedPane jTabbedpane = new JTabbedPane();// 存放选项卡的组件
        ImageIcon icon = new ImageIcon();
        for(String name:workBlockMap.keySet()){
            JPanel jpanel = createResultPanel(allRuntimeData,berthId,name);
            jTabbedpane.addTab("结果："+name, icon,jpanel,name);
        }
        setLayout(new GridLayout(1, 1));
        add(jTabbedpane);

    }

    public JPanel createResultPanel(AllRuntimeData allRuntimeData, Long berthId,String resultName){
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel,BoxLayout.Y_AXIS));
        resultPanel.add(createHatchBox(allRuntimeData,berthId,resultName));
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(createWorkBlock(resultName));
        return resultPanel;
    }



    //hatchPanel->buttonPanel->miniPanel
    public JPanel createHatchBox(final AllRuntimeData allRuntimeData, Long berthId, final String resultName){
        JPanel hatchPanel = new JPanel();
        hatchPanel.setLayout(new BoxLayout(hatchPanel,BoxLayout.Y_AXIS));
        hatchPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        shipWidth = (int)Math.ceil((hatchList.size()+2)*(hatchList.get(0).getHatchLength()*ratio+28));
//        System.out.println("shipWidth--"+shipWidth);
        hatchPanel.setPreferredSize(new Dimension(shipWidth,60));
        JLabel shipName = new JLabel("BerthId:"+berthId.toString()+"   "+"VesselCode:"+schedule.getVesselCode()+"   "+"Direction:"+schedule.getPlanBerthDirect());
        hatchPanel.add(shipName);
        JPanel buttonPanel = new JPanel();
        FlowLayout buttonLayout = new FlowLayout(FlowLayout.LEFT,8,0);
        buttonPanel.setLayout(buttonLayout);
        //数据
        Collections.sort(hatchList,new Comparator<VMHatch>(){

            @Override
            public int compare(VMHatch o1, VMHatch o2) {
                if(schedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L)){
                    if(o1.getBayNo1()<=o2.getBayNo1()){
                        return -1;
                    }else{
                        return 1;
                    }
                }else{
                    if(o1.getBayNo1()<=o2.getBayNo1()){
                        return 1;
                    }else{
                        return -1;
                    }
                }
            }
        });
        Double prePosition = 0.0;
        Double preHatchLength = 0.0;
        int len = 0;
        for(final VMHatch hatch:hatchList){
            JPanel miniPanel = new JPanel();
            FlowLayout miniLayout = new FlowLayout(FlowLayout.LEFT,4,0);
            miniPanel.setLayout(miniLayout);
            Border titleBorder1 = BorderFactory.createTitledBorder(hatch.getHatchId().toString());
            miniPanel.setBorder(titleBorder1);
            for(SmartVpsVslHatchsInfo hatchsInfo:smartVpsVslHatchsInfoList){
                if(hatchsInfo.getHatchId().equals(hatch.getHatchId())){
                    hatchLength = hatchsInfo.getHatchLength();
                }
            }
            String str1 = null;
            Double position = hatch.getHatchPosition();
            len = Math.abs((int)Math.ceil(position - prePosition));
            if(len>hatchLength+5 && !prePosition.equals(0.0)){
                int bL = (int)Math.ceil(hatchLength*ratio);
                JPanel mPanel = new JPanel();
                FlowLayout mLayout = new FlowLayout(FlowLayout.LEFT,4,0);
                mPanel.setLayout(mLayout);
                mPanel.setBorder(BorderFactory.createTitledBorder("machine"));
                JButton b = new JButton("机械设备");
                b.setPreferredSize(new Dimension(bL,20));
                mPanel.add(b);
                buttonPanel.add(mPanel);
            }
            prePosition = position;
            preHatchLength = hatchLength;
            if(schedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L)){
                if(hatch.getBayNo1()<10){
                   str1 = "0"+hatch.getBayNo1();
                }else{
                    str1 = hatch.getBayNo1().toString();
                }
            }else{
                if(hatch.getBayNo2()<10){
                    str1 = "0"+hatch.getBayNo2();
                }else{
                    str1 = hatch.getBayNo2().toString();
                }
            }

            int buttonLength =(int)Math.ceil(hatchLength*ratio/2);

            JButton b1 = new JButton(str1);
            b1.setPreferredSize(new Dimension(buttonLength,20));
            b1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Long hatchId = hatch.getHatchId();
                    HatchFramByResult hatchFrame = new HatchFramByResult(GlobalData.selectedBerthId, hatchId, CWPDomain.DL_TYPE_DISC, allRuntimeData,resultName);
                    hatchFrame.setVisible(true);
                    HatchFramByResult hatchFrame1 = new HatchFramByResult(GlobalData.selectedBerthId, hatchId, CWPDomain.DL_TYPE_LOAD, allRuntimeData,resultName);
                    hatchFrame1.setVisible(true);
                }
            });
            miniPanel.add(b1);
            String str2 = null;
            if(schedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L)){
                if(hatch.getBayNo2()<10){
                    str2 = "0"+hatch.getBayNo2();
                }else{
                    str2 = hatch.getBayNo2().toString();
                }
            }else{
                if(hatch.getBayNo1()<10){
                    str2 = "0"+hatch.getBayNo1();
                }else{
                    str2 = hatch.getBayNo1().toString();
                }
            }
            JButton b2 = new JButton(str2);
            b2.setPreferredSize(new Dimension(buttonLength,20));
            b2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Long hatchId = hatch.getHatchId();
                    HatchFramByResult hatchFrame = new HatchFramByResult(GlobalData.selectedBerthId, hatchId, CWPDomain.DL_TYPE_DISC, allRuntimeData,resultName);
                    hatchFrame.setVisible(true);
                    HatchFramByResult hatchFrame1 = new HatchFramByResult(GlobalData.selectedBerthId, hatchId, CWPDomain.DL_TYPE_LOAD, allRuntimeData,resultName);
                    hatchFrame1.setVisible(true);
                }
            });
            miniPanel.add(b2);
            buttonPanel.add(miniPanel);
        }
        hatchPanel.add(buttonPanel);
        return hatchPanel;
    }

    public JPanel createWorkBlock(String resultName){
        JPanel workBlockPanel = new WorkBlockPanel(resultName);
        return workBlockPanel;
    }

    class WorkBlockPanel extends JPanel{

        private String resultName;

        public WorkBlockPanel(String resultName){
            this.resultName = resultName;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "作业块"));
            setBackground(Color.white);
            this.setPreferredSize(new Dimension(0, ship.getPlanHeight() + head + bold / 2+300));
        }

        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            if (ship.getLR().equals("L")) {
                RL = "L";
            }
            if (ship.getLR().equals("R")) {
                RL = "R";
            }

            //画舱和倍位信息
            Map<Integer, Integer> bayPositionQuery = new HashMap<>();
            g2d.setStroke(new BasicStroke(1));
            List<Long> hatchIds = structureData.getAllHatchIdList();
            if(RL.equals("L")){
                Collections.sort(hatchIds);
            }else{
                Collections.reverse(hatchIds);
            }

            Double prePosition = 0.0;
            int i = 0;
            //8*2+4*3=28
            int param = 30;
            int startL = 0;
            int pWidth = 0;
            int pStart = 0;
            int pMiddle =0;
            int pEnd = 0;
            for (Long hatchId : hatchIds) {
                VMHatch vmHatch = structureData.getVMHatchByHatchId(hatchId);
                int hatchX = vmHatch.getHatchPosition().intValue();
                if(startL == 0 && RL.equals("L")){
                    startL = hatchX * ratio-13;
                }else if(pWidth == 0 && RL.equals("R")){
//                    pWidth = hatchX*ratio+param*(hatchIds.size()+machineNum);
                    pWidth = hatchX*ratio+12;
                    System.out.println("pWidth--"+pWidth);
                }
                //检测机器设备
                Double position = structureData.getVMHatchByHatchId(hatchId).getHatchPosition();
                int len = (int)Math.ceil(position - prePosition);
                if(len>hatchLength && !prePosition.equals(0.0)){
                    i++;
                }
                prePosition = position;
                if(RL.equals("L")){
                    pStart = getXPosition(RL,hatchX * ratio-startL+param*i,pWidth);
                    pMiddle = getXPosition(RL,(int)Math.ceil(hatchX * ratio-startL+param*i+hatchLength*ratio/2+6),pWidth);
                    pEnd = getXPosition(RL,(int)Math.ceil(hatchX * ratio-startL+param*i+hatchLength*ratio+12),pWidth);
                }else if(RL.equals("R")){
//                    System.out.println("舱"+vmHatch.getHatchId()+"--"+hatchX);
                    pStart = getXPosition(RL,(int)Math.ceil(hatchX * ratio-param*i),pWidth);
//                    System.out.println("pStart--"+pStart);
                    pMiddle = getXPosition(RL,(int)Math.ceil(hatchX * ratio-param*i-hatchLength*ratio/2-6),pWidth);
                    pEnd = getXPosition(RL,(int)Math.ceil(hatchX * ratio-param*i-hatchLength*ratio-12),pWidth);
//                    System.out.println("---"+pEnd);
                }

                //画舱开始坐标线
                g2d.setColor(Color.BLACK);
                g2d.drawLine(pStart, head, pStart, ship.getPlanHeight());
//                System.out.println(vmHatch.getHatchId()+"--"+pStart);
//                画舱中心虚线
                g2d.setPaint(Color.LIGHT_GRAY);
                g2d.drawLine(pMiddle, head, pMiddle, ship.getPlanHeight());
                //画舱结束坐标线
                g2d.setColor(Color.BLUE);
//                hatchX = hatchX + vmHatch.getHatchLength().intValue();
                g2d.drawLine(pEnd, head, pEnd, ship.getPlanHeight());
                //保存作业块中心线位置
                Integer bayNo1 = vmHatch.getBayNo1();
                int bayP1 = (pStart+pMiddle)/2;
                bayPositionQuery.put(bayNo1, bayP1);
//                System.out.println(bayNo1+"--"+bayP1);

                Integer bayNo3 = vmHatch.getBayNo2();
                int bayP3 = (pMiddle+pEnd)/2;
                bayPositionQuery.put(bayNo3, bayP3);
//                System.out.println(bayNo3+"--"+bayP3);

                Integer bayNo2 = (bayNo1+bayNo3)/2;
                int bayP2 = pMiddle;
                bayPositionQuery.put(bayNo2, bayP2);
                i++;
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
            Map<String, List<WorkBlock>> allWorkBlockMap = new HashMap<String, List<WorkBlock>>();
            Integer j = 0;
            for (Long berthId : ship.getAllRuntimeData().getAllBerthId()) {
                for(List<WorkBlock> workBlockList: ship.getAllRuntimeData().getWorkingDataByBerthId(berthId).getWorkBlockMap().values()){
                    allWorkBlockMap.put(j.toString(),workBlockList);
                    j++;
                }
            }
            List<WorkBlock> workBlocks= allWorkBlockMap.get(resultName);
            for (WorkBlock workBlock : workBlocks) {
                String craneNo = workBlock.getCraneNo();//得到桥机号
                int bayNo = Integer.valueOf(workBlock.getBayNo());//得到倍位号
                Long startTime = workBlock.getWorkingStartTime().getTime() / 1000 - ship.getMinTime();
                Long endTime = workBlock.getWorkingEndTime().getTime() / 1000 - ship.getMinTime();
                Long moveCount = workBlock.getPlanAmount();
                Long craneSeq = workBlock.getCraneSeq();
                Long hatchSeq = workBlock.getHatchSeq();
                String ldFlag = workBlock.getLduldfg();
                if (countQuery.get(bayNo) != null) {
                    countQuery.put(bayNo, countQuery.get(bayNo) + moveCount);
                } else {
                    countQuery.put(bayNo, moveCount);
                }
                int w = bayNo % 2 == 0 ? blockWidthD : blockWidthX;//作业块宽度
//                int x = getXPosition(RL, bayPositionQuery.get(bayNo),pWidth) - w / 2 + 3;
                int x = bayPositionQuery.get(bayNo) - w / 2 + 3;
                int y = head + startTime.intValue() * (timeBlock) / ship.getMaxTime().intValue()+20;
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
                if(ldFlag != null){
                    g2d.drawString(ldFlag, mx1, my);
                }else{
                    g2d.drawString(" ", mx1, my);
                }
            }
        }
    }


    private int getXPosition(String rl, int po,int pWidth) {
        if (rl.equals("L")) {
            return po;
        } else {
//            return ship.getShipWidth() - po;
            return pWidth - po;
        }
    }

    private int getOpposite(String rl, int n) {
        if (rl.equals("L")) {
            return n;
        } else {
            return 0 - n;
        }
    }
}
