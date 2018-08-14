package com.cwp3.data;

import com.cwp3.data.all.MachineData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.log.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AllRuntimeData {

    private Logger logger;
    private MachineData machineData;              //所有的机械设备数据

    //单船数据
    private Map<String, StructureData> structureDataMap; //船舶结构,key: vesselCode
    private Map<Long, WorkingData> workingDataMap; //作业数据,key: berthId

    //计算过程中生成的结果
    private Map<String, Object> storageMap;

    public AllRuntimeData() {
        this.machineData = new MachineData();
        this.structureDataMap = new HashMap<>();
        this.workingDataMap = new HashMap<>();
        this.storageMap = new HashMap<>();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MachineData getMachineData() {
        return machineData;
    }

    public void addStructData(StructureData structureData) {
        structureDataMap.put(structureData.getVesselCode(), structureData);
    }

    public StructureData getStructDataByVesselCode(String vesselCode) {
        return structureDataMap.get(vesselCode);
    }

    public void addWorkingData(WorkingData workingData) {
        workingDataMap.put(workingData.getVmSchedule().getBerthId(), workingData);
    }

    public WorkingData getWorkingDataByBerthId(Long berthId) {
        return workingDataMap.get(berthId);
    }

    public void putStorage(String key, Object object) {
        storageMap.put(key, object);
    }

    public Object getStorageByKey(String key) {
        return storageMap.get(key);
    }

    public Collection<StructureData> getStructureDateValues(){
        return structureDataMap.values();
    }

    public Collection<String> getAllVesselCode(){
        return structureDataMap.keySet();
    }

    public Collection<Long> getAllBerthId(){
        return workingDataMap.keySet();
    }
}
