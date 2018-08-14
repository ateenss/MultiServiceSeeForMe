package com.cwp.test.cwpData.test18_4_16;

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
public class Test4_17 {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws ParseException {

        String filePath = "CWP3Test/cwpData/18.4.16/4.17/CWP霍斯沙克尔顿出口航次：815N进口航次：815NI-20180417110056620/";

        String cwpInfo = FileUtil.readFileToString(new File(filePath + "cwpImportDataJson.txt")).toString();

        Gson gson = new GsonBuilder().create();
        SmartCwpImportData cwpImportData = gson.fromJson(cwpInfo, SmartCwpImportData.class);

        ImportDataFrameAllShip importDataFrame = new ImportDataFrameAllShip(cwpImportData);
        importDataFrame.setVisible(true);
    }
}
