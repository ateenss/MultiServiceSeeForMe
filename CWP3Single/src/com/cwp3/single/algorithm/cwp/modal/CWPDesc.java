package com.cwp3.single.algorithm.cwp.modal;

/**
 * Created by csw on 2018/6/27.
 * Description:
 */
public enum  CWPDesc {

    canNotWork(0, "不能选择该倍位（物理移动限制/没有可作业量）"),
    outWorkRange(1, "桥机平均作业量划分范围之外"),

    inWorkRange(2, "桥机平均作业量划分范围之内"),
    firstSelectFactor(3, "倍位作业量因素（第一次决策）"),
//    splitRoad(4, "并列三个舱选择劈路作业原则"),
//    separateBay(5, "两部桥机分界倍位优先选择作业"),
//    specialBay(6, "特殊倍位优先作业：船头作业困难、只跨一次驾驶台作业"),

    lastSelectBay(4, "桥机保持在上次选择的倍位中作业"),
    steppingCntFirst(5, "同一个舱内垫脚箱可以连续做完时，优先选择作业");
//    avoidKeyRoad(8, "避免形成新的重点路");

//    public static final String keyRoad = "保证重点路优先作业";

    private int code;
    private String desc;

    CWPDesc(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
