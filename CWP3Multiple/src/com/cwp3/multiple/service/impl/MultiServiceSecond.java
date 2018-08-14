package com.cwp3.multiple.service.impl;

import com.cwp.test.util.FileUtil;
import com.cwp3.model.crane.CMCrane;
import com.cwp3.model.crane.CMCranePool;
import com.cwp3.model.crane.CMVesselCranePool;
import com.cwp3.model.log.Logger;
import com.cwp3.model.vessel.VMSchedule;
import com.cwp3.multiple.service.impl.datamodule.AllData;
import com.cwp3.multiple.service.impl.datamodule.CwpMethod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;
import com.shbtos.biz.smart.cwp.service.SmartCwpResults;

import java.io.File;
import java.util.*;

/**
 * @author dongyuhang
 * @date 2018/8/14 12:58
 * @Description:
 */
public class MultiServiceSecond {

    public static void main(String args[]) {
        String filePath = "E:\\project\\CWP3.ALLVESSEL\\data\\";
        //读入cwpImportDataJson.txt数据
        //FileUtil类在CWP3Test.src.util.FileUtil
        String cwpInfo = FileUtil.readFileToString(new File(filePath + "cwpImportDataJson.txt")).toString();
        //Gson这里需要看一下
        Gson gson = new GsonBuilder().create();
        //导入所有cwp需要的参数,是gson直接从Json文件按照java类，自动装配生成一个类
        SmartCwpImportData cwpImportData = gson.fromJson(cwpInfo, SmartCwpImportData.class);
        SmartCwpResults cwpResults = new SmartCwpResults();
        //内部代码没有写AllData类
        AllData allData = new AllData();
        CwpMethod.parseAllData(allData, cwpImportData, cwpResults);   //解析所有的Json数据
        AllVesselData allVesselData = generateAllVesselData(allData);
        doMultiCwp(allData, allVesselData);       //对多条船进行船机资源调度
        //得到当前船桥机池对应信息
        Logger allLogger = allData.getLogger();
        List<SingleData>singleDataList=allData.getAllSingleDataList();
        for(SingleData singleData:singleDataList){
            CMVesselCranePool cmVesselCranePool=singleData.getCmVesselCranePool();
            allLogger.logInfo("船舶berthId："+cmVesselCranePool.getBerthId()+"  对应桥机池："+cmVesselCranePool.getPoolId());
        }
        //对应的桥机池分配信息
        for(SingleData singleData:singleDataList){
            List<CMCranePool> cmCranePoolList=singleData.getAllCMCranePoolList();
            for(CMCranePool cmCranePool:cmCranePoolList){
                System.out.println("桥机："+cmCranePool.getCraneNo()+"  存放于桥机池："+cmCranePool.getPoolId()+"开始工作时间:"+cmCranePool.getWorkStartTime()+"结束工作时间:"+cmCranePool.getWorkEndTime());
            }
        }
    }





    public static AllVesselData generateAllVesselData(AllData alldata) {
        AllVesselData allVesselData = new AllVesselData();
        List<SingleData> singleDataList = alldata.getAllSingleDataList(); //单船信息链表
        Map<Long, SingleData> singleDataMap = new HashMap<>(); //单船信息map
        Map<Long, SingleVesselData> singleVesselDataMap = new HashMap<>();
        //SingleVesselDataList构造
        List<SingleVesselData> singleVesselDataList = new ArrayList<>();
        //这边singlevesseldata和singledata都建造了一个list和一个map对象
        for (SingleData singleData : singleDataList) {
            VMSchedule vmSchedule = singleData.getVmSchedule();  //通过singledata可以拿到船期信息
            long berthId = vmSchedule.getBerthId();  //船期信息里面还包含有靠泊ID
            Schedule schedule = new Schedule(vmSchedule, 0); //组装新的船舶计划表
            SingleVesselData singleVesselData = new SingleVesselData(schedule);
            singleVesselDataList.add(singleVesselData);//不断地制造singlevesseldata，然后放入到list容器里面
            singleDataMap.put(berthId, singleData);         //建立SingData和SingleVesselData之间的联系
            singleVesselDataMap.put(berthId, singleVesselData);
        }
        //新造出来的allVesselData在这里吧上述三种东西全都装进来
        allVesselData.setSingleDataMap(singleDataMap);
        allVesselData.setSingleVesselDataMap(singleVesselDataMap);
        allVesselData.setSingleVesselDataList(singleVesselDataList);

        //Crane对象的构造，桥机开始出现了
        List<CMCrane> cmCraneList = alldata.getAllCMCranes(); //采用接口传进来的参数制作桥机list，讲主函数解析出来的总桥机数量进行提取
        Map<String, CMCrane> cmCraneMap = new HashMap<>();
        Map<String, Crane> tempMap = new HashMap<>();
        List<Crane> craneList = new ArrayList<>();   //那么cmcrane和crane到底区别到底在哪里，他这样做的目的又是在哪里呢？
        for (CMCrane cmCrane : cmCraneList) {//开始遍历桥机cmCraneMap装的是原始cmCrane
            String craneNo = cmCrane.getCraneNo();
            Crane crane = new Crane(cmCrane, 0);//采用cmCrane重新包装制作了一个Crane
            craneList.add(crane);
            cmCraneMap.put(craneNo, cmCrane); //
            tempMap.put(craneNo, crane);//tempMap里面装的是包装之后的桥机
        }
        allVesselData.setCmCraneMap(cmCraneMap); //前面制造的allVesselData继续装入原始桥机容器
        //对桥机按照编号进行排序
        //桥机真的是按照顺序号编序号的吗？？？感觉真是排班表里面并没有按照这个顺序号进行编写
        Collections.sort(craneList, new Comparator<Crane>() {
            @Override
            public int compare(Crane o1, Crane o2) {
                int flag = o1.getCmCrane().getCraneNo().compareTo(o2.getCmCrane().getCraneNo());
                return flag;
            }
        });
        //排序
        List<Map.Entry<String,Crane>> tempList =
                new ArrayList<Map.Entry<String,Crane>>(tempMap.entrySet()); //将整个entry取出来之后，再放入到list里面进行包装，在放入的过程中
        //按照entry，也就会放入的collection的自己itorater进行默认排序
        Collections.sort(tempList, new Comparator<Map.Entry<String, Crane>>() {
            public int compare(Map.Entry<String,Crane> o1, Map.Entry<String,Crane> o2) {
                return o1.getKey().compareTo(o2.getKey()); //对刚才组装好的list进行重新自定义排序，顺序按照key的大小进行
            }
        });
        Map<String,Crane>craneMap=new LinkedHashMap<String,Crane>();  //LinkedHashMap保存插入的顺序，LinkedHashMap相比HashMap可以保持插入顺序
        for(int i=0;i<tempList.size();i++)
        {
            String key=tempList.get(i).getKey();
            Crane value=tempList.get(i).getValue();
            craneMap.put(key,value);  //将原来的entry的号码还有值取出之后，进行一次map的重新组合
        }
        allVesselData.setCraneList(craneList);
        allVesselData.setCraneMap(craneMap); //将刚组装好的map还有list（都是有关桥机的），这俩都是包装桥机之后的容器，
        //然后吧这两个处理好的容器类直接放入到allVesselData，仍然是这个allVesselData，前面都已经放入很多东西
        return allVesselData;
        //allvesseldata里面包装了大量的桥机和桥机号
    }
}
