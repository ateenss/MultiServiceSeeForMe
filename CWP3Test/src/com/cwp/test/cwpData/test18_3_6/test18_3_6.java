package com.cwp.test.cwpData.test18_3_6;

import com.cwp.test.ViewFrame.ImportDataFrameAllShip;
import com.cwp.test.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by CarloJones on 2018/6/18.
 */
public class test18_3_6 {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws ParseException {

        String filePath = "CWP3Test/cwpData/18.3.6/3.6/CWP塞斯潘大洋洲出口航次：SF810A进口航次：SF806R-20180306142035402/";

        String cwpInfo = FileUtil.readFileToString(new File(filePath + "cwpImportDataJson.txt")).toString();

        Gson gson = new GsonBuilder().create();
        SmartCwpImportData cwpImportData = gson.fromJson(cwpInfo, SmartCwpImportData.class);

        ImportDataFrameAllShip importDataFrame = new ImportDataFrameAllShip(cwpImportData);
        importDataFrame.setVisible(true);
    }
}
