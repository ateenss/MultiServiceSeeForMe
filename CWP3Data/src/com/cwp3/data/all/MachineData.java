package com.cwp3.data.all;

import com.cwp3.model.crane.CMCrane;
import com.cwp3.model.crane.CMCraneType;

import java.util.*;

public class MachineData {

    private Map<String, CMCraneType> cmCraneTypeMap;        //桥机类型,key: qcTypeId
    private Map<String, CMCrane> cmCraneMap;      //桥机对象,key: craneNo

    public MachineData() {
        cmCraneTypeMap = new HashMap<>();
        cmCraneMap = new HashMap<>();
    }

    public void addCMCrane(CMCrane cmCrane) {
        cmCraneMap.put(cmCrane.getCraneNo(), cmCrane);
    }

    public CMCrane getCMCraneByCraneNo(String craneNo) {
        return cmCraneMap.get(craneNo);
    }

    public void addCMCraneType(CMCraneType cmCraneType) {
        cmCraneTypeMap.put(cmCraneType.getCraneTypeId(), cmCraneType);
    }

    public List<CMCraneType> getCMCraneTypes() {
        return new ArrayList<>(cmCraneTypeMap.values());
    }

    public CMCraneType getCMCraneTypeById(String cmCraneTypeId) {
        return cmCraneTypeMap.get(cmCraneTypeId);
    }
}
