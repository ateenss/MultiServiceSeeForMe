package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.ioservice.ParseDataService;
import com.cwp3.ioservice.impl.ResultGeneratorServiceImpl;
import com.cwp3.ioservice.impl.ParseDataServiceImpl;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.single.data.MoveData;
import com.cwp3.single.method.MoveDataMethod;
import com.cwp3.single.service.CwpService;
import com.cwp3.single.service.HatchBlockService;
import com.cwp3.single.service.MoveService;
import com.cwp3.single.service.impl.CwpServiceImpl;
import com.cwp3.single.service.impl.HatchBlockServiceImpl;
import com.cwp3.single.service.impl.MoveServiceImpl;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpBlockInfo;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpCraneEfficiencyInfo;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpModalInfo;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpWorkOrderInfo;
import com.shbtos.biz.smart.cwp.pojo.*;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2016/12/13 14:48.
 * Explain:
 */
public class ImportDataFrameAllShip extends JFrame {

    private JPanel mainPanel = null;
    private JPanel btnPanel = null;
    private CardLayout card = null;
    private JButton voyCraneBnt = null, vesselStructBnt = null, preStowBnt = null, cwpResultBnt;
    private JDesktopPane vesselStructPane = null, preStowPane = null;
    private JDesktopPane voyCranePane = null;
    private JDesktopPane cwpResultPane = null;

    private JButton executeBnt, moBnt, reWorkBnt;
    private JLabel selectVesselLabel;
    private JLabel selectVessel;
    private String vesselCodeStr;
    private String berthIdStr;

    private AllRuntimeData allRuntimeData;
    private SmartCwpImportData smartCwpImportData;
    private SmartCwp3Results smartCwpResults;

    private ResultGeneratorServiceImpl generateResultService;

    public ImportDataFrameAllShip(SmartCwpImportData smartCwpImportData) {
        this.smartCwpImportData = smartCwpImportData;
        generateResultService = new ResultGeneratorServiceImpl();
        initComponents();
    }

    private void initComponents() {

        this.setTitle("算法数据导入界面");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(GlobalData.width, GlobalData.height);
        this.setResizable(true);
        this.setLocationRelativeTo(null);// 居中显示

        selectVesselLabel = new JLabel("选择了哪条船: ");
        selectVessel = new JLabel("");

        moBnt = new JButton("生成作业工艺");
        moBnt.setBackground(Color.BLUE);
        moBnt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GlobalData.selectedBerthId != null) {
                    smartCwpResults = new SmartCwp3Results(); //创建结果对象

                    ParseDataService parseDataService = new ParseDataServiceImpl();
                    allRuntimeData = parseDataService.parseAllRuntimeData(smartCwpImportData);
                    HatchBlockService hatchBlockService = new HatchBlockServiceImpl();
                    hatchBlockService.makeHatchBlock(allRuntimeData, GlobalData.selectedBerthId);
                    MoveService moveService = new MoveServiceImpl();
                    moveService.makeWorkFlow(allRuntimeData, GlobalData.selectedBerthId, null);

                    MoveDataMethod moveDataMethod = new MoveDataMethod();
                    MoveData moveData = moveDataMethod.initMoveData(allRuntimeData, GlobalData.selectedBerthId);
                    moveDataMethod.initCurTopWorkMove(moveData, allRuntimeData.getWorkingDataByBerthId(GlobalData.selectedBerthId),
                            allRuntimeData.getStructDataByVesselCode(allRuntimeData.getWorkingDataByBerthId(GlobalData.selectedBerthId).getVmSchedule().getVesselCode()));
                    moveService.calculateMoves(allRuntimeData, GlobalData.selectedBerthId, moveData);

                    new ResultGeneratorServiceImpl().generateCwpResult(moveData, allRuntimeData.getWorkingDataByBerthId(GlobalData.selectedBerthId));
                    System.out.println();

                } else {
                    System.out.println("请选择相应的航次信息！");
                }
            }
        });

        executeBnt = new JButton("调用CWP算法");
        executeBnt.setBackground(Color.GREEN);
        executeBnt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (GlobalData.selectedBerthId != null) {
                    smartCwpResults = new SmartCwp3Results(); //创建结果对象

                    ParseDataService parseDataService = new ParseDataServiceImpl();
                    allRuntimeData = parseDataService.parseAllRuntimeData(smartCwpImportData);
                    HatchBlockService hatchBlockService = new HatchBlockServiceImpl();
                    hatchBlockService.makeHatchBlock(allRuntimeData, GlobalData.selectedBerthId);
                    MoveService moveService = new MoveServiceImpl();
                    moveService.makeWorkFlow(allRuntimeData, GlobalData.selectedBerthId, null);

                    CwpService cwpService = new CwpServiceImpl();
                    cwpService.doPlanCwp(allRuntimeData, GlobalData.selectedBerthId);

                    generateResultService.generateCwpResult1(allRuntimeData.getWorkingDataByBerthId(GlobalData.selectedBerthId), smartCwpResults);

                    ResultFrameAllShip resultFrameAllShip = new ResultFrameAllShip(smartCwpImportData, allRuntimeData, GlobalData.PLAN);
                    WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(GlobalData.selectedBerthId);
                    for (Map.Entry<String, List<WorkBlock>> entry : workingData.getWorkBlockMap().entrySet()) {
                        ResultFrameAllShip1 resultFrameAllShip1 = new ResultFrameAllShip1(allRuntimeData, GlobalData.selectedBerthId, entry.getValue(), GlobalData.PLAN);
                    }

                    //----CWP结果信息----
                    for (SmartReCwpModalInfo smartReCwpModalInfo : smartCwpResults.getSmartReCwpModalInfoList()) {
                        BaseFrame cwpOrderResultInfoFrame = new BaseFrame("CWP结果" + smartReCwpModalInfo.getModalName(), SmartReCwpWorkOrderInfo.class, smartReCwpModalInfo.getSmartReCwpWorkOrderInfoList());
                        cwpOrderResultInfoFrame.setVisible(true);
                        cwpResultPane.add(cwpOrderResultInfoFrame);

                        BaseFrame cwpBlockResultInfoFrame = new BaseFrame("CWP作业块结果" + smartReCwpModalInfo.getModalName(), SmartReCwpBlockInfo.class, smartReCwpModalInfo.getSmartReCwpBlockInfoList());
                        cwpBlockResultInfoFrame.setVisible(true);
                        cwpResultPane.add(cwpBlockResultInfoFrame);

                        BaseFrame craneEffResultInfoFrame = new BaseFrame("桥机评价" + smartReCwpModalInfo.getModalName(), SmartReCwpCraneEfficiencyInfo.class, smartReCwpModalInfo.getSmartReCwpCraneEfficiencyInfoList());
                        craneEffResultInfoFrame.setVisible(true);
                        cwpResultPane.add(craneEffResultInfoFrame);
                    }

                } else {
                    System.out.println("请选择相应的航次信息！");
                }

            }
        });

        reWorkBnt = new JButton("重排CWP");
        reWorkBnt.setBackground(Color.CYAN);
        reWorkBnt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

////                ResultFrameAllShip resultFrameAllShip = new ResultFrameAllShip(smartCwpImportData, GlobalData.REWORK);
////                resultFrameAllShip.setVisible(true);
//
//                //----CWP结果信息----
//                BaseFrame cwpOrderResultInfoFrame = new BaseFrame("CWP结果", SmartReCwpWorkOrderInfo.class, smartCwpResults.getSmartReCwpWorkOrderInfoList());
//                cwpOrderResultInfoFrame.setVisible(true);
//                cwpResultPane.add(cwpOrderResultInfoFrame);
//                BaseFrame cwpBlockResultInfoFrame = new BaseFrame("CWP作业块结果", SmartReCwpBlockInfo.class, smartCwpResults.getSmartReCwpBlockInfoList());
//                cwpBlockResultInfoFrame.setVisible(true);
//                cwpResultPane.add(cwpBlockResultInfoFrame);
//                try {
//                    cwpBlockResultInfoFrame.setIcon(true);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
            }
        });

        voyCraneBnt = new JButton("航次桥机");
        vesselStructBnt = new JButton("船舶结构");
        preStowBnt = new JButton("预配数据");
        cwpResultBnt = new JButton("CWP结果");

        voyCraneBnt.setMargin(new Insets(2, 2, 2, 2));
        vesselStructBnt.setMargin(new Insets(2, 2, 2, 2));
        preStowBnt.setMargin(new Insets(2, 2, 2, 2));

        btnPanel = new JPanel();
        btnPanel.setBackground(Color.LIGHT_GRAY);
        btnPanel.add(selectVesselLabel);
        btnPanel.add(selectVessel);
        btnPanel.add(voyCraneBnt);
        btnPanel.add(vesselStructBnt);
        btnPanel.add(preStowBnt);
        btnPanel.add(moBnt);
        btnPanel.add(executeBnt);
        btnPanel.add(reWorkBnt);
        btnPanel.add(cwpResultBnt);

        card = new CardLayout(0, 0);
        mainPanel = new JPanel(card);
        voyCranePane = new JDesktopPane();
        vesselStructPane = new JDesktopPane();
        preStowPane = new JDesktopPane();
        cwpResultPane = new JDesktopPane();
        voyCranePane.setBackground(Color.LIGHT_GRAY);
        vesselStructPane.setBackground(Color.CYAN);
        preStowPane.setBackground(Color.GRAY);
        cwpResultPane.setBackground(Color.GRAY);
        mainPanel.add(voyCranePane, "p1");
        mainPanel.add(vesselStructPane, "p2");
        mainPanel.add(preStowPane, "p3");
        mainPanel.add(cwpResultPane, "p4");
        voyCraneBnt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                card.show(mainPanel, "p1");
            }
        });
        vesselStructBnt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                card.show(mainPanel, "p2");
            }
        });
        preStowBnt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                card.show(mainPanel, "p3");
            }
        });
        cwpResultBnt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(mainPanel, "p4");
            }
        });

        //----航次桥机信息----
        final BaseFrame scheduleInfoFrame = new BaseFrame("航次信息", SmartScheduleIdInfo.class, smartCwpImportData.getSmartScheduleIdInfoList());
        scheduleInfoFrame.setSize(GlobalData.width - 50, GlobalData.height - 400);
        scheduleInfoFrame.setVisible(true);
        //通过选择航次，改变全局传入数据
        scheduleInfoFrame.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = scheduleInfoFrame.table;
                if (e.getClickCount() == 1) {
                    vesselCodeStr = table.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex("vesselCode")).toString();
                    berthIdStr = table.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex("berthId")).toString();
                    selectVessel.setText(vesselCodeStr);
                    GlobalData.selectedVesselCode = vesselCodeStr;
                    GlobalData.selectedBerthId = Long.valueOf(berthIdStr);
                    GlobalData.noticeValueChanged();
                    //得到相应船舶的数据
                } else if (e.getClickCount() == 2) {
                    selectVessel.setText("");
                    berthIdStr = "";
                    GlobalData.selectedVesselCode = "";
                    GlobalData.selectedBerthId = null;
                    GlobalData.noticeValueChanged();
                }
            }
        });
        voyCranePane.add(scheduleInfoFrame);
        BaseFrame craneBaseInfoFrame = new BaseFrame("桥机信息", SmartCraneBaseInfo.class, smartCwpImportData.getSmartCraneBaseInfoList());
        craneBaseInfoFrame.setVisible(true);
        voyCranePane.add(craneBaseInfoFrame);
        BaseFrame cranePoolInfoFrame = new BaseFrame("桥机池信息", SmartCranePoolInfo.class, smartCwpImportData.getSmartCranePoolInfoList());
        cranePoolInfoFrame.setVisible(true);
        voyCranePane.add(cranePoolInfoFrame);
        BaseFrame vesselCranePoolInfoFrame = new BaseFrame("船舶桥机池信息", SmartVesselCranePoolInfo.class, smartCwpImportData.getSmartVesselCranePoolInfoList());
        vesselCranePoolInfoFrame.setVisible(true);
        voyCranePane.add(vesselCranePoolInfoFrame);
        BaseFrame craneMaintainInfoFrame = new BaseFrame("桥机维修计划信息", SmartCraneMaintainPlanInfo.class, smartCwpImportData.getSmartCraneMaintainPlanInfoList());
        craneMaintainInfoFrame.setVisible(true);
        voyCranePane.add(craneMaintainInfoFrame);
        BaseFrame cranePlanInfoFrame = new BaseFrame("桥机计划工作信息", SmartCranePlanInfo.class, smartCwpImportData.getSmartCranePlanInfoList());
        cranePlanInfoFrame.setVisible(true);
        voyCranePane.add(cranePlanInfoFrame);
        BaseFrame cwpConfigurationInfFrame = new BaseFrame("新算法配置参数", SmartCwpParameterInfo.class, smartCwpImportData.getSmartCwpParameterInfoList());
        cwpConfigurationInfFrame.setSize(GlobalData.width - 50, GlobalData.height - 600);
        cwpConfigurationInfFrame.setVisible(true);
        voyCranePane.add(cwpConfigurationInfFrame);

        //----船舶结构信息----
        BaseFrame hatchInfoFrame = new BaseFrame("舱信息", SmartVpsVslHatchsInfo.class, smartCwpImportData.getSmartVpsVslHatchsInfoList());
        hatchInfoFrame.setVisible(true);
        vesselStructPane.add(hatchInfoFrame);
        BaseFrame bayInfoFrame = new BaseFrame("倍信息", SmartVpsVslBaysInfo.class, smartCwpImportData.getSmartVpsVslBaysInfoList());
        bayInfoFrame.setVisible(true);
        vesselStructPane.add(bayInfoFrame);
        BaseFrame rowInfoFrame = new BaseFrame("排信息", SmartVpsVslRowsInfo.class, smartCwpImportData.getSmartVpsVslRowsInfoList());
        rowInfoFrame.setVisible(true);
        vesselStructPane.add(rowInfoFrame);
        BaseFrame locationInfoFrame = new BaseFrame("船箱位信息", SmartVpsVslLocationsInfo.class, smartCwpImportData.getSmartVpsVslLocationsInfoList());
        locationInfoFrame.setVisible(true);
        vesselStructPane.add(locationInfoFrame);
        BaseFrame machineInfoFrame = new BaseFrame("船舶机械信息", SmartVesselMachinesInfo.class, smartCwpImportData.getSmartVesselMachinesInfoList());
        machineInfoFrame.setVisible(true);
        vesselStructPane.add(machineInfoFrame);
        BaseFrame hatchCoverInfoFrame = new BaseFrame("舱盖板信息", SmartVpsVslHatchcoversInfo.class, smartCwpImportData.getSmartVpsVslHatchcoversInfoList());
        hatchCoverInfoFrame.setVisible(true);
        vesselStructPane.add(hatchCoverInfoFrame);

        try {
            craneBaseInfoFrame.setIcon(true);
            cranePoolInfoFrame.setIcon(true);
            vesselCranePoolInfoFrame.setIcon(true);
            craneMaintainInfoFrame.setIcon(true);
            cranePlanInfoFrame.setIcon(true);
            cwpConfigurationInfFrame.setIcon(true);
            bayInfoFrame.setIcon(true);
            rowInfoFrame.setIcon(true);
            locationInfoFrame.setIcon(true);
            machineInfoFrame.setIcon(true);
            hatchCoverInfoFrame.setIcon(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        //----预配信息-----
        final BaseFrame containerInfoFrame = new BaseFrame("船箱信息", SmartVesselContainerInfo.class, smartCwpImportData.getSmartVesselContainerInfoList());
        containerInfoFrame.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = containerInfoFrame.table;
                if (e.getClickCount() == 2) {
                    Long hatchId = Long.valueOf(table.getValueAt(table.getSelectedRow(), table.getColumnModel().getColumnIndex("hatchId")).toString());
                    HatchFrame hatchFrame = new HatchFrame(GlobalData.selectedBerthId, hatchId, CWPDomain.DL_TYPE_DISC, allRuntimeData);
                    hatchFrame.setVisible(true);
                    HatchFrame hatchFrame1 = new HatchFrame(GlobalData.selectedBerthId, hatchId, CWPDomain.DL_TYPE_LOAD, allRuntimeData);
                    hatchFrame1.setVisible(true);
                }
            }
        });
        containerInfoFrame.setVisible(true);
        preStowPane.add(containerInfoFrame);
        BaseFrame lockLocationInfoFrame = new BaseFrame("锁定船箱位信息", SmartStowageLockLocationsInfo.class, smartCwpImportData.getSmartStowageLockLocationsInfoList());
        lockLocationInfoFrame.setVisible(true);
        preStowPane.add(lockLocationInfoFrame);
        BaseFrame areaCntInfoFrame = new BaseFrame("箱区统计信息", SmartAreaContainerInfo.class, smartCwpImportData.getSmartAreaContainerInfoList());
        areaCntInfoFrame.setVisible(true);
        preStowPane.add(areaCntInfoFrame);
        BaseFrame groupInfoFrame = new BaseFrame("属性组信息", SmartContainerGroupInfo.class, smartCwpImportData.getSmartContainerGroupInfoList());
        groupInfoFrame.setVisible(true);
        preStowPane.add(groupInfoFrame);
        BaseFrame workflowInfoFrame = new BaseFrame("作业工艺信息", SmartCraneWorkFlowInfo.class, smartCwpImportData.getSmartCraneWorkFlowInfoList());
        workflowInfoFrame.setVisible(true);
        preStowPane.add(workflowInfoFrame);

        try {
            lockLocationInfoFrame.setIcon(true);
            areaCntInfoFrame.setIcon(true);
            groupInfoFrame.setIcon(true);
            workflowInfoFrame.setIcon(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.getContentPane().add(mainPanel);
        this.getContentPane().add(btnPanel, BorderLayout.SOUTH);

    }
}
