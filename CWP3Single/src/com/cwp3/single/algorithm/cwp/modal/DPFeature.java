package com.cwp3.single.algorithm.cwp.modal;

import java.io.*;

/**
 * Created by csw on 2017/12/13.
 * Description:
 */
public class DPFeature implements Serializable {

//    private int canNotWork = 0; //物理移动范围之外不能作业
//    private int outWorkRange = 1; //桥机平均作业量划分范围之外
//
//    private int inWorkRange = 2; //倍位作业量因素
//    private int splitRoad = 3; //并列三个舱选择劈路作业原则
//    private int separateBay = 4; //两部桥机分界倍位优先选择作业
//    private int specialBay = 5; //特殊倍位优先作业：船头作业困难、只跨一次驾驶台作业、装/卸错开开路、。。。
//
//    private int lastSelectBay = 6; //桥机上次选择的倍位优先作业
//    private int avoidKeyRoad = 7; //避免形成新的重点路，其中包括垫脚箱不及时作业会形成重点路、同时旁边桥机停靠等待的情况？？？
//    private int keyRoad = 8; //保证重点路优先作业

    private int code;
    private String desc;

    public DPFeature(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public DPFeature deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            return (DPFeature) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
