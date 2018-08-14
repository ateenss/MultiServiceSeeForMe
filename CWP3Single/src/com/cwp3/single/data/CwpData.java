package com.cwp3.single.data;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.utils.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class CwpData {

    private WorkingData workingData;
    private StructureData structureData;

    private Map<String, CWPCrane> cwpCraneMap;
    private Map<Integer, CWPBay> cwpBayMap;
    private Map<Integer, CWPBay> machineBayMap;
    private Long cwpStartTime;

    private MoveData moveData; //深复制
    private MoveResults moveResults;

    private Boolean firstDoCwp;
    private Long dpCurrentTime;
    private DPResult dpResult;
    private List<List<CWPCrane>> dpFirstCwpCraneList; //Analyzer->Evaluator
    private List<CWPCrane> dpCwpCraneList;
    private List<DPCraneSelectBay> dpCraneSelectBays;
    private Integer dpMoveNumber;

    public CwpData(WorkingData workingData, StructureData structureData) {
        this.workingData = workingData;
        this.structureData = structureData;
        cwpCraneMap = new HashMap<>();
        cwpBayMap = new HashMap<>();
        machineBayMap = new HashMap<>();
        moveResults = new MoveResults();
        firstDoCwp = Boolean.TRUE;
        dpCwpCraneList = new ArrayList<>();
        dpResult = new DPResult();
        dpCraneSelectBays = new ArrayList<>();
        dpFirstCwpCraneList = new ArrayList<>();
        dpMoveNumber = 0;
    }

    public WorkingData getWorkingData() {
        return workingData;
    }

    public StructureData getStructureData() {
        return structureData;
    }

    public Long getVesselTime() {
        return DateUtil.getSecondTime(workingData.getVmSchedule().getPlanEndWorkTime()) - dpCurrentTime - 3600;
    }

    public Long getCwpStartTime() {
        return cwpStartTime;
    }

    public void setCwpStartTime(Long cwpStartTime) {
        this.cwpStartTime = cwpStartTime;
    }

    public void addCWPCrane(CWPCrane cwpCrane) {
        cwpCraneMap.put(cwpCrane.getCraneNo(), cwpCrane);
    }

    public CWPCrane getCWPCraneByCraneNo(String craneNo) {
        return cwpCraneMap.get(craneNo);
    }

    public List<CWPCrane> getAllCWPCranes() {
        List<CWPCrane> cwpCraneList = new ArrayList<>(cwpCraneMap.values());
        PublicMethod.sortCWPCraneByCraneSeq(cwpCraneList);
        return cwpCraneList;
    }

    public void addCWPBay(CWPBay cwpBay) {
        cwpBayMap.put(cwpBay.getBayNo(), cwpBay);
    }

    public CWPBay getCWPBayByBayNo(Integer bayNo) {
        return cwpBayMap.get(bayNo);
    }

    public List<CWPBay> getAllCWPBays() {
        List<CWPBay> cwpBayList = new ArrayList<>(cwpBayMap.values());
        PublicMethod.sortCWPBayByWorkPosition(cwpBayList);
        return cwpBayList;
    }

    public void addMachineBay(CWPBay cwpBay) {
        machineBayMap.put(cwpBay.getBayNo(), cwpBay);
    }

    public CWPBay getMachineBayByBayNo(Integer bayNo) {
        return machineBayMap.get(bayNo);
    }

    public List<CWPBay> getAllMachineBays() {
        List<CWPBay> machineBayList = new ArrayList<>(machineBayMap.values());
        PublicMethod.sortCWPBayByWorkPosition(machineBayList);
        return machineBayList;
    }

    public MoveData getMoveData() {
        return moveData;
    }

    public void setMoveData(MoveData moveData) {
        this.moveData = moveData;
    }

    public MoveResults getMoveResults() {
        return moveResults;
    }

    public void setMoveResults(MoveResults moveResults) {
        this.moveResults = moveResults;
    }

    public List<DPCraneSelectBay> getDpCraneSelectBays() {
        return dpCraneSelectBays;
    }

    public void setDpCraneSelectBays(List<DPCraneSelectBay> dpCraneSelectBays) {
        this.dpCraneSelectBays = dpCraneSelectBays;
    }

    public Long getDpCurrentTime() {
        return dpCurrentTime;
    }

    public void setDpCurrentTime(Long dpCurrentTime) {
        this.dpCurrentTime = dpCurrentTime;
    }

    public DPResult getDpResult() {
        return dpResult;
    }

    public void setDpResult(DPResult dpResult) {
        this.dpResult = dpResult;
    }

    public List<CWPCrane> getDpCwpCraneList() {
        return dpCwpCraneList;
    }

    public void setDpCwpCraneList(List<CWPCrane> dpCwpCraneList) {
        this.dpCwpCraneList = dpCwpCraneList;
    }

    public Boolean getFirstDoCwp() {
        return firstDoCwp;
    }

    public void setFirstDoCwp(Boolean firstDoCwp) {
        this.firstDoCwp = firstDoCwp;
    }

    public List<List<CWPCrane>> getDpFirstCwpCraneList() {
        return dpFirstCwpCraneList;
    }

    public void setDpFirstCwpCraneList(List<List<CWPCrane>> dpFirstCwpCraneList) {
        this.dpFirstCwpCraneList = dpFirstCwpCraneList;
    }

    public Integer getDpMoveNumber() {
        return dpMoveNumber;
    }

    public void setDpMoveNumber(Integer dpMoveNumber) {
        this.dpMoveNumber = dpMoveNumber;
    }
}
