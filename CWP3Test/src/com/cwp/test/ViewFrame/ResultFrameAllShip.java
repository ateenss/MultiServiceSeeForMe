package com.cwp.test.ViewFrame;

import com.cwp3.data.AllRuntimeData;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import javax.swing.*;
import java.awt.*;

/**
 * Created by csw on 2016/12/15 12:29.
 * Explain:
 */
public class ResultFrameAllShip extends JFrame {

    private JScrollPane scrollPane;

    private AllRuntimeData allRuntimeData;
    private String type;
    private SmartCwpImportData smartCwpImportData;

    public ResultFrameAllShip(SmartCwpImportData smartCwpImportData, AllRuntimeData allRuntimeData, String type) {
        this.allRuntimeData = allRuntimeData;
        this.type = type;
        this.smartCwpImportData = smartCwpImportData;
        initComponents();
    }

    private void initComponents() {
        this.setTitle("算法结果页面");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(GlobalData.reWidth, GlobalData.reHeight);
        this.setResizable(true);

        ResultPanelAllShip panel = new ResultPanelAllShip(smartCwpImportData ,allRuntimeData, type);

        scrollPane = new JScrollPane(panel);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.getContentPane().add(scrollPane);
        this.setVisible(true);
    }
}
