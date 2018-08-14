package com.cwp3.domain;

/**
 * Created by csw on 2017/4/24 15:58.
 * Explain:
 */
public class CWPDefaultValue {

    public static final String VERSION = "CWP3.0.8.9";
    public static boolean outputLogToConsole = true;

    public static Integer twinWeightDiff = 100000; //重量差100吨，即默认没有重量差

    public static Long oneCntWorkTime = 144L;
    public static Long keyBayWorkTime = 6 * 3600L; //21600
    public static Long dividedBayWorkTime = 2 * 3600L;
    public static Long keepSelectedBayWorkTime = 10 * 3600L;
    public static Double machineHeight = 15.0;
    public static Boolean crossBridge = true;
    public static Boolean crossChimney = true;
    public static Long crossBarTime = 900L;
    public static Double craneSafeSpan = 14.0;
    public static Double craneSpeed = 0.75;//m/s
    public static Long delCraneTimeParam = 1800L;
    public static Long addCraneTimeParam = 1800L;
    public static Long amount = 15L;
    public static Long breakDownCntTime = 30 * 60L;
    public static Long autoDelCraneAmount = 15L;
    public static Long steppingCntWaitTime = 17 * 60L; //15分钟

    public static Boolean steppingCnt = true;
    public static Boolean keyBay = true; //大船的情况重点路权重对结果没什么影响
    public static Boolean dividedBay = true;
    public static Boolean curWorkVesselBay = true;
    public static Boolean keepLastSelectBay = true; //一般情况下是为true
    public static Boolean strictDividedBay = true; //一般情况下是为true
    public static Boolean keepOneHatchWork = true; //一般情况下是为true
    public static Boolean changeToLastSelectBay = true; //多数情况是要的，可以在一定程度上减少桥机的无故换倍作业，减少移动
    public static Boolean changeSideCraneWork = true;
    public static Boolean autoDeleteCrane = true; //自己测试用，一般情况下是为true
    public static Boolean keepMaxRoadBay = true; //自己测试用，一般情况下是为true
    public static Boolean divideByMaxRoad = false;
    public static Boolean changeDpBySteppingCnt = true;
    public static Boolean changeDpByLoadSteppingCnt = true;

    public static long closeToScheduleTime = 1800;
    public static long negligibleTime = 16 * 120;

    public static Long hatchCoverOpenTime = 240L;
    public static Long hatchCoverCloseTime = 240L;
    public static Long unlockTwistTime = 90L;
    public static Long single20Time = 120L;
    public static Long single20FootPadTime = 180L;
    public static Long single20SeparateTime = 180L;
    public static Long single40Time = 120L;
    public static Long single45Time = 120L;
    public static Long double20Time = 150L;
    public static Long double40Time = 140L;
    public static Long double45Time = 140L;
    public static Long specialCntTime = 360L;
    public static Long dangerCntTime = 360L;
    public static Double impactFactor = 1.0D;
    public static Long hatchScanTime = 300L;
    public static String craneSameWorkTime = "N";
    public static String deckWorkLater = "N";
    public static String mainRoadOneCrane = "N";
    public static String dividedHatchFirst = "N";
}
