package com.cwp3.single.method;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCrane;
import com.cwp3.model.crane.CMCranePool;
import com.cwp3.model.log.Logger;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMHatch;
import com.cwp3.model.vessel.VMMachine;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.CraneEfficiency;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.algorithm.move.MoveCalculator;
import com.cwp3.single.data.CwpData;
import com.cwp3.single.data.MoveData;
import com.cwp3.utils.*;

import java.util.*;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class CwpDataMethod {

    private AllRuntimeData allRuntimeData;
    private MoveCalculator moveCalculator;
    private MoveDataMethod moveDataMethod;

    public CwpDataMethod(AllRuntimeData allRuntimeData) {
        this.allRuntimeData = allRuntimeData;
        moveCalculator = new MoveCalculator();
        moveDataMethod = new MoveDataMethod();
    }

    public CwpData initCwpData(Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        CwpData cwpData = new CwpData(workingData, allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode()));
        Logger logger = workingData.getLogger();
        try {
            logger.logInfo("The CwpData bay(machines) is being initialized...");
            this.initBayInfo(cwpData);
            logger.logInfo("The CwpData crane is being initialized...");
            this.initCraneInfo(cwpData);
            logger.logInfo("The CwpData startTime is being initialized...");
            this.initCwpDataTime(cwpData);
            logger.logInfo("The CwpData moveData is being initialized...");
            MoveData moveData = moveDataMethod.initMoveData(allRuntimeData, berthId);
            cwpData.setMoveData(moveData);
        } catch (Exception e) {
            logger.logError("初始化算法数据(CwpData)过程中发生异常！");
            e.printStackTrace();
        }
        return cwpData;
    }

    private void initBayInfo(CwpData cwpData) {
        Logger logger = cwpData.getWorkingData().getLogger();
        //真实舱信息
        List<Long> hatchIds = cwpData.getStructureData().getAllHatchIdList();
        for (Long hatchId : hatchIds) {
            VMHatch vmHatch = cwpData.getStructureData().getVMHatchByHatchId(hatchId);
            try {
                //bay
                List<Integer> bayNos = vmHatch.getAllBayNos();
                for (Integer bayNo : bayNos) {
                    Double bayHatchPo = vmHatch.getVMBayPosition(bayNo);
                    double bayPo = cwpData.getWorkingData().getBayPosition(bayHatchPo);
                    CWPBay cwpBay = new CWPBay(bayNo, hatchId, bayPo);
                    cwpData.addCWPBay(cwpBay);
                }
            } catch (Exception e) {
                logger.logError("计算舱(hatchId:" + hatchId + ")内每个倍位作业位置的方法发生(空指针)异常！");
                e.printStackTrace();
                break;
            }
        }
        //船舶机械模拟成一个舱信息
        List<VMMachine> vmMachineList = cwpData.getStructureData().getAllVMMachineList();
        for (int i = 0; i < vmMachineList.size(); i++) {
            VMMachine vmMachine = vmMachineList.get(i);
            try {
                Integer bayNo = -(i + 1);
                Double bayHatchPo = vmMachine.getMachinePosition();
                double bayPo = cwpData.getWorkingData().getBayPosition(bayHatchPo);
                CWPBay cwpBay = new CWPBay(bayNo, (long) bayNo, bayPo);
                cwpBay.setBayType(CWPDomain.BAY_TYPE_VIRTUAL);
                cwpData.addMachineBay(cwpBay);
            } catch (Exception e) {
                logger.logError("计算船舶器械(machineNo:" + vmMachine.getMachineNo() + ")位置坐标的方法发生(空指针)异常！");
                e.printStackTrace();
                break;
            }
        }
    }

    private void initCraneInfo(CwpData cwpData) {
        for (CMCranePool cmCranePool : cwpData.getWorkingData().getAllCMCranePools()) {
            CMCrane cmCrane = allRuntimeData.getMachineData().getCMCraneByCraneNo(cmCranePool.getCraneNo());
            cwpData.getWorkingData().getLogger().logError("根据桥机号(craneNo: " + cmCranePool.getCraneNo() + ")找不到相应桥机信息！", ValidatorUtil.isNull(cmCrane));
            CWPCrane cwpCrane = new CWPCrane(cmCranePool.getCraneNo());
            cwpCrane = (CWPCrane) BeanCopyUtil.copyBean(cmCrane, cwpCrane);
            cwpCrane.setDpCurrentWorkPosition(cwpCrane.getCurrentCranePosition()); //桥机当前位置
            cwpCrane.setFirstWorkBayNo(cmCranePool.getFirstWorkBayNo());
            cwpCrane.setFirstWorkAmount(cmCranePool.getFirstWorkAmount());
            cwpData.addCWPCrane(cwpCrane);
        }
//        //模拟参数
//        Integer[] bayNos = new Integer[]{14, 38, 54, 70,94};
//        for (int i = 0; i < cwpData.getAllCWPCranes().size(); i++) {
//            CWPCrane cwpCrane = cwpData.getAllCWPCranes().get(i);
//            cwpCrane.setFirstWorkBayNo(bayNos[i]);
//        }
    }

    private void initCwpDataTime(CwpData cwpData) {
        long startTime = DateUtil.getSecondTime(cwpData.getWorkingData().getVmSchedule().getPlanBeginWorkTime());
        if (cwpData.getWorkingData().getCwpType().equals(CWPDomain.CWP_TYPE_WORK)) {
            //todo:重排算法要考虑桥机继续完成已发指令所消耗的时间
            startTime = DateUtil.getSecondTime(new Date());
        }
        cwpData.setCwpStartTime(startTime);
        cwpData.setDpCurrentTime(startTime);
    }

    public void computeCurrentWorkTime(CwpData cwpData) {
        if (cwpData.getDpResult().getDpTraceBack().size() == 0) {
            List<Long> hatchIdList = cwpData.getStructureData().getAllHatchIdList();
            for (Long hatchId : hatchIdList) {
                computeWorkTimeByHatchId(hatchId, cwpData);
            }
        } else {
            for (DPPair dpPair : cwpData.getDpResult().getDpTraceBack()) {
                Long hatchId = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond()).getHatchId();
                computeWorkTimeByHatchId(hatchId, cwpData);
            }
        }
    }

    private void computeWorkTimeByHatchId(Long hatchId, CwpData cwpData) {
        //初始化顶层
        moveDataMethod.initCurTopWorkMoveByHatchId(hatchId, cwpData.getMoveData(), cwpData.getStructureData());
        //计算总量（先计算）、可作业量
        moveCalculator.calculateTotalMove(hatchId, cwpData.getMoveData(), cwpData.getMoveResults(), cwpData.getStructureData());
        moveCalculator.calculateAvailableMove(hatchId, cwpData.getMoveData(), cwpData.getMoveResults(), cwpData.getWorkingData(), cwpData.getStructureData());
    }

    public CwpData copyCwpData(CwpData cwpData) {
        CwpData cwpDataCopy = new CwpData(cwpData.getWorkingData(), cwpData.getStructureData());
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) {
            cwpDataCopy.addCWPCrane(cwpCrane);
        }
        for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
            cwpDataCopy.addCWPBay(cwpBay);
        }
        for (CWPBay cwpBay : cwpData.getAllMachineBays()) {
            cwpDataCopy.addMachineBay(cwpBay);
        }
        cwpDataCopy.setCwpStartTime(cwpData.getCwpStartTime());
        cwpDataCopy.setMoveData(moveDataMethod.copyMoveData(cwpData.getMoveData()));
        cwpDataCopy.setMoveResults(cwpData.getMoveResults().deepCopy());
        cwpDataCopy.setDpCurrentTime(cwpData.getDpCurrentTime());
        cwpDataCopy.setDpResult(cwpData.getDpResult());
        for (CWPCrane cwpCrane : cwpData.getDpCwpCraneList()) {
            cwpDataCopy.getDpCwpCraneList().add(cwpCrane.deepCopy());
        }
        cwpDataCopy.setFirstDoCwp(cwpData.getFirstDoCwp());
        cwpDataCopy.setDpMoveNumber(cwpData.getDpMoveNumber());
        return cwpDataCopy;
    }

    public long doProcessOrder(CWPCrane cwpCrane, CWPBay cwpBay, String selectReason, long realMinWorkTime, CwpData cwpData) {
        long wt = 0;
        Long craneStartTime = cwpCrane.getDpCurrentTime();
        if (cwpBay.getDpAvailableWorkTime() - realMinWorkTime < cwpData.getWorkingData().getCwpConfig().getSingle20FootPadTime()) {
            realMinWorkTime = cwpBay.getDpAvailableWorkTime();
        }
        Map<Integer, List<WorkMove>> availableWorkMoveMap = cwpData.getMoveResults().getAvailableWorkMoveMapByBayNo(cwpBay.getBayNo());
        List<WorkMove> workMoveList = new ArrayList<>();
        for (Map.Entry<Integer, List<WorkMove>> entry : availableWorkMoveMap.entrySet()) {
            workMoveList.addAll(entry.getValue());
        }
        PublicMethod.sortWorkMoveListByMoveOrder(workMoveList);
//        for (List<WorkMove> workMoveList : availableWorkMoveMap.values()) { //分档进行编序
            for (WorkMove workMove : workMoveList) {
                WorkMove workMove1 = cwpData.getMoveData().getWorkMoveByVMSlot(workMove.getOneVMSlot(), workMove.getDlType());
                if (wt < realMinWorkTime) {
                    long curCost = workMove1.getWorkTime();
                    workMove1.setPlanStartTime(new Date(craneStartTime * 1000));
                    workMove1.setPlanEndTime(new Date((craneStartTime + curCost) * 1000));
                    workMove1.setCraneNo(cwpCrane.getCraneNo());
                    workMove1.setWorkPosition(cwpBay.getWorkPosition());
                    workMove1.setSelectReason(selectReason);
                    workMove1.setMoveOrder(workMove.getMoveOrder());
                    wt += curCost;
                    craneStartTime += curCost;
                    cwpData.getMoveData().setCurMoveOrder(cwpBay.getHatchId(), workMove.getMoveOrder() + 1);
                    cwpData.getMoveData().setCurWorkFlow(cwpBay.getHatchId(), workMove.getWorkFlow());
                } else { //超过最小作业时间
//                    workMove1.setMoveOrder(null);
                    break;
                }
            }
//        }
        //清除这个舱内其它倍位的moveOrder
//        for (Integer bayNo : cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getAllBayNos()) {
//            if (!bayNo.equals(cwpBay.getBayNo())) {
//                Map<Integer, List<WorkMove>> availableWorkMoveMap1 = cwpData.getMoveResults().getAvailableWorkMoveMapByBayNo(bayNo);
//                for (List<WorkMove> workMoves : availableWorkMoveMap1.values()) { //分档进行编序
//                    for (WorkMove workMove : workMoves) {
//                        WorkMove workMove1 = cwpData.getMoveData().getWorkMoveByVMSlot(workMove.getOneVMSlot(), workMove.getDlType());
//                        workMove1.setMoveOrder(null);
//                    }
//                }
//            }
//        }
        return wt;
    }

    public void generateResult(List<CwpData> cwpDataResultList) {
        for (int i = 0; i < cwpDataResultList.size(); i++) {
            String key = String.valueOf(i);
            CwpData cwpData = cwpDataResultList.get(i);
            Set<WorkMove> workMoveSet = new HashSet<>();
            workMoveSet.addAll(cwpData.getMoveData().getDiscWorkMoveMap().values());
            workMoveSet.addAll(cwpData.getMoveData().getLoadWorkMoveMap().values());
            cwpData.getWorkingData().getWorkMoveMap().put(key, new ArrayList<>(workMoveSet));
            List<WorkBlock> workBlockList = generateWorkBlock(cwpData.getWorkingData().getWorkMoveMap().get(key));
            cwpData.getWorkingData().getWorkBlockMap().put(key, workBlockList);
            List<CraneEfficiency> craneEfficiencyList = generateCraneEfficiency(cwpData.getWorkingData().getWorkMoveMap().get(key), cwpData);
            cwpData.getWorkingData().getCraneEfficiencyMap().put(key, craneEfficiencyList);
        }
    }

    private List<CraneEfficiency> generateCraneEfficiency(List<WorkMove> workMoveList, CwpData cwpData) {
        List<CraneEfficiency> craneEfficiencyList = new ArrayList<>();
        Map<String, List<WorkMove>> workMoveMap = new HashMap<>();
        for (WorkMove workMove : workMoveList) {
            if (workMoveMap.get(workMove.getCraneNo()) == null) {
                workMoveMap.put(workMove.getCraneNo(), new ArrayList<WorkMove>());
            }
            workMoveMap.get(workMove.getCraneNo()).add(workMove);
        }
        for (Map.Entry<String, List<WorkMove>> entry : workMoveMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                Collections.sort(entry.getValue(), new Comparator<WorkMove>() {
                    @Override
                    public int compare(WorkMove o1, WorkMove o2) {
                        return o1.getPlanStartTime().compareTo(o2.getPlanStartTime());
                    }
                });
                CraneEfficiency craneEfficiencyL = new CraneEfficiency();
                craneEfficiencyL.setCraneNo(entry.getKey());
                craneEfficiencyL.setDlType(CWPDomain.DL_TYPE_LOAD);
                craneEfficiencyL = createCraneEfficiency(entry.getValue(), craneEfficiencyL, cwpData);
                CraneEfficiency craneEfficiencyD = new CraneEfficiency();
                craneEfficiencyD.setCraneNo(entry.getKey());
                craneEfficiencyD.setDlType(CWPDomain.DL_TYPE_DISC);
                craneEfficiencyD = createCraneEfficiency(entry.getValue(), craneEfficiencyD, cwpData);
                craneEfficiencyList.add(craneEfficiencyL);
                craneEfficiencyList.add(craneEfficiencyD);
            }
        }
        return craneEfficiencyList;
    }

    private CraneEfficiency createCraneEfficiency(List<WorkMove> workMoveList, CraneEfficiency craneEfficiency, CwpData cwpData) {
        craneEfficiency.setBerthId(cwpData.getWorkingData().getVmSchedule().getBerthId());
        for (WorkMove workMove : workMoveList) {
            if (craneEfficiency.getDlType().equals(workMove.getDlType())) {
                if (workMove.getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    Set<VMContainer> vmContainerSet = new HashSet<>();
                    for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                        VMContainer vmContainer = cwpData.getWorkingData().getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                        vmContainerSet.add(vmContainer);
                    }
                    craneEfficiency.addPlanWorkTime(workMove.getWorkTime().doubleValue());
                    for (VMContainer vmContainer : vmContainerSet) {
                        craneEfficiency.addPlanWorkCntNumber(1);
                        if (CWPDomain.YES.equals(vmContainer.getDgCd())) {
                            craneEfficiency.addDangerCntNumber(1);
                        }
                        if (CWPDomain.YES.equals(vmContainer.getOverrunCd())) {
                            craneEfficiency.addOverLimitCntNumber(1);
                        }
                    }
                }
            }
        }
        if (craneEfficiency.getPlanWorkTime() > 0) {
            craneEfficiency.setPlanWorkTime(CalculateUtil.div(craneEfficiency.getPlanWorkTime(), 3600, 2));
            craneEfficiency.setPlanWorkEfficiency(CalculateUtil.div(craneEfficiency.getPlanWorkCntNumber().doubleValue(), craneEfficiency.getPlanWorkTime(), 2));
        }
        if (craneEfficiency.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
            craneEfficiency.setDiscCntNumber(craneEfficiency.getPlanWorkCntNumber());
        }
        if (craneEfficiency.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
            craneEfficiency.setLoadCntNumber(craneEfficiency.getPlanWorkCntNumber());
        }
        return craneEfficiency;
    }

    private List<WorkBlock> generateWorkBlock(List<WorkMove> workMoveList) {
        List<WorkBlock> workBlockList = new ArrayList<>();
        Map<String, List<WorkMove>> allMap = new HashMap<>();
        for (WorkMove workMove : workMoveList) {
            if (workMove.getWorkPosition() != null) { //有作业位置
                Integer bayNo = workMove.getBayNo();
                String craneNo = workMove.getCraneNo();
                String cKey = StringUtil.getKey(bayNo, craneNo);     //倍位号.桥机号.装卸
                //将数据按key为倍位号.桥机号.装卸，保存在Map里，其中value为是以开始时间为key的Map，目的是为了后面以时间顺序组成大作业块
                if (allMap.get(cKey) == null) {
                    allMap.put(cKey, new ArrayList<WorkMove>());
                }
                allMap.get(cKey).add(workMove);
            }
        }
        for (Map.Entry<String, List<WorkMove>> entry : allMap.entrySet()) {
            PublicMethod.sortWorkMoveListByPlanStartTime(entry.getValue());
            WorkMove workMoveLast = new WorkMove();
            for (int i = 0; i < entry.getValue().size(); i++) {
                WorkMove workMoveCur = entry.getValue().get(i);
                if (workMoveLast.getDlType() == null) {
                    workMoveLast = copyWorkMove(workMoveCur, workMoveLast);
                } else {
                    long cur_last_time = workMoveCur.getPlanStartTime().getTime() / 1000 - workMoveLast.getPlanEndTime().getTime() / 1000;
                    if (cur_last_time == 0 && workMoveCur.getMoveType().equals(workMoveLast.getMoveType())) {
                        workMoveLast.setPlanEndTime(workMoveCur.getPlanEndTime());
                        workMoveLast.setMoveOrder(workMoveLast.getMoveOrder() + 1);
                    } else {//不是连续时间片
                        WorkBlock workBlock = new WorkBlock();
                        workBlock = createWorkBlock(workBlock, workMoveLast);
                        workBlockList.add(workBlock);
                        workMoveLast = copyWorkMove(workMoveCur, workMoveLast);
                    }
                }
            }
            if (workMoveLast != null) {
                WorkBlock workBlock = new WorkBlock();
                workBlock = createWorkBlock(workBlock, workMoveLast);
                workBlockList.add(workBlock);
            }
        }
        return workBlockList;
    }

    private WorkMove copyWorkMove(WorkMove workMoveCur, WorkMove workMoveLast) {
        workMoveLast.setDlType(workMoveCur.getDlType());
        workMoveLast.setCraneNo(workMoveCur.getCraneNo());
        workMoveLast.setBayNo(workMoveCur.getBayNo());
        workMoveLast.setMoveOrder(1L);
        workMoveLast.setMoveType(workMoveCur.getMoveType());
        workMoveLast.setHatchId(workMoveCur.getHatchId());
        workMoveLast.setWorkPosition(workMoveCur.getWorkPosition());
        workMoveLast.setPlanStartTime(workMoveCur.getPlanStartTime());
        workMoveLast.setPlanEndTime(workMoveCur.getPlanEndTime());
        workMoveLast.setSelectReason(workMoveCur.getSelectReason());
        return workMoveLast;
    }

    private WorkBlock createWorkBlock(WorkBlock workBlock, WorkMove workMove) {
        workBlock.setCraneNo(workMove.getCraneNo());
        workBlock.setBayNo(String.format("%02d", workMove.getBayNo()));
        workBlock.setHatchId(workMove.getHatchId());
        workBlock.setPlanAmount(workMove.getMoveOrder());
        workBlock.setCranePosition(workMove.getWorkPosition());
        workBlock.setLduldfg(workMove.getDlType());
        workBlock.setWorkingStartTime(workMove.getPlanStartTime());
        workBlock.setWorkingEndTime(workMove.getPlanEndTime());
        workBlock.setSelectReason(workMove.getSelectReason());
        workBlock.setBlockType(workMove.getMoveType());
        return workBlock;
    }
}
