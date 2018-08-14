package com.cwp.test.cwpData1.test17_10_17;

import com.cwp.test.ViewFrame.ImportDataFrameAllShip;
import com.cwp.test.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class Test10_19 {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws ParseException {

        String filePath = "CWP3Test/cwpData1/10.17/10.19/CWP中远北京出口航次：1019E进口航次：1019I-20171019161811678/";

        String cwpInfo = FileUtil.readFileToString(new File(filePath + "cwpImportDataJson.txt")).toString();

        Gson gson = new GsonBuilder().create();
        SmartCwpImportData cwpImportData = gson.fromJson(cwpInfo, SmartCwpImportData.class);

        ImportDataFrameAllShip importDataFrame = new ImportDataFrameAllShip(cwpImportData);
        importDataFrame.setVisible(true);
    }
}
