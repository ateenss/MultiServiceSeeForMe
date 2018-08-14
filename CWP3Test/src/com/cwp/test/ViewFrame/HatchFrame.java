package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;

import javax.swing.*;
import java.awt.*;

/**
 * Created by csw on 2017/2/22 19:12.
 */
public class HatchFrame extends JFrame {

    private Long berthId;
    private Long hatchId;
    private String dlType;
    private AllRuntimeData allRuntimeData;

    private JScrollPane scrollPane;

    public HatchFrame(Long berthId, Long hatchId, String dlType, AllRuntimeData allRuntimeData) {
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

        HatchPanel1 hatchPanel = new HatchPanel1(berthId, hatchId, dlType, allRuntimeData);

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
