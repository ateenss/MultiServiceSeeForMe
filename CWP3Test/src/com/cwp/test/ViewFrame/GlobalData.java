package com.cwp.test.ViewFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yc on 2018/3/14.
 */
public class GlobalData {

    public static Integer ratio = 1;

    public static Integer width = 1300;//主窗口宽
    public static Integer height = 850;//主窗口高

    public static Integer hatchWidth = 1700;
    public static Integer hatchHeight = 1000;

    public static Integer reWidth = 25000;//cwp结果图界面宽
    public static Integer reHeight = 900;//高

    public static String selectedVesselCode = null;
    public static Long selectedBerthId = null;

    public static String REWORK = "REWORK";
    public static String PLAN = "PLAN";

    private static List<IValueChangeListener> iValueChangeListenerList = new ArrayList<>();

    public static void addIValueChangeListener(IValueChangeListener iValueChangeListener){
        iValueChangeListenerList.add(iValueChangeListener);
    }

    public static void noticeValueChanged(){
        for (IValueChangeListener iValueChangeListener:iValueChangeListenerList){
            iValueChangeListener.valueChanged();
        }
    }

}
