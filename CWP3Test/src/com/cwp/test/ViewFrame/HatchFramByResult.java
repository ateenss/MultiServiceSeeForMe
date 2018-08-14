package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;

import javax.swing.*;
import java.awt.*;

/**
 * Created by CarloJones on 2018/7/11.
 */
public class HatchFramByResult extends JFrame{
    private Long berthId;
    private Long hatchId;
    private String dlType;
    private AllRuntimeData allRuntimeData;

    private JScrollPane scrollPane;
    private String resultName;

    public HatchFramByResult(Long berthId, Long hatchId, String dlType, AllRuntimeData allRuntimeData,String resultName) {
        this.resultName = resultName;
        this.berthId = berthId;
        this.hatchId = hatchId;
        this.dlType = dlType;
        this.allRuntimeData = allRuntimeData;
        initComponents();
    }

    public void initComponents() {
        this.setTitle("舱" + hatchId + "倍位图-" + dlType);
        this.setSize(GlobalData.hatchWidth, GlobalData.hatchHeight);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        this.setLocationRelativeTo(null);// 居中显示

//        HatchPanel1ByResult hatchPanel = new HatchPanel1ByResult(berthId, hatchId, dlType, allRuntimeData,resultName);
        HatchPanelByResult hatchPanel = new HatchPanelByResult(berthId, hatchId, dlType, allRuntimeData,resultName);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(hatchPanel, gridBagConstraints);
    }
}
