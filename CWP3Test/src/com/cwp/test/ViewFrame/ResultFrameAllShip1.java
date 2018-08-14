package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.model.work.WorkBlock;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import javax.swing.*;
import java.util.List;

/**
 * Created by csw on 2016/12/15 12:29.
 * Explain:
 */
public class ResultFrameAllShip1 extends JFrame {

    private JScrollPane scrollPane;

    private AllRuntimeData allRuntimeData;
    private Long berthId;
    private List<WorkBlock> workBlockList;
    private String type;

    public ResultFrameAllShip1(AllRuntimeData allRuntimeData, Long berthId, List<WorkBlock> workBlockList, String type) {
        this.allRuntimeData = allRuntimeData;
        this.berthId = berthId;
        this.workBlockList = workBlockList;
        this.type = type;
        initComponents();
    }

    private void initComponents() {
        this.setTitle("算法结果页面");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(GlobalData.reWidth, GlobalData.reHeight);
        this.setResizable(true);

        ResultPanelAllShip1 panel = new ResultPanelAllShip1(allRuntimeData, berthId, workBlockList, type);

        scrollPane = new JScrollPane(panel);
        this.getContentPane().add(scrollPane);
        this.setVisible(true);
    }
}
