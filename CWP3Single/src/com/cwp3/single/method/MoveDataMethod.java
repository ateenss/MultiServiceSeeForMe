package com.cwp3.single.method;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;
import com.cwp3.utils.StringUtil;

import java.util.*;

/**
 * Created by csw on 2018/5/31.
 * Description:
 */
public class MoveDataMethod {

    public MoveData initMoveData(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        MoveData moveData = new MoveData();
        moveData.setDiscWorkMoveMap(this.copyWorkMoveMap(workingData.getDiscWorkMoveMap()));
        moveData.setLoadWorkMoveMap(this.copyWorkMoveMap(workingData.getLoadWorkMoveMap()));
        return moveData;
    }

    public void initCurTopWorkMove(MoveData moveData, WorkingData workingData, StructureData structureData) {
        List<Long> hatchIdList = structureData.getAllHatchIdList();
        for (Long hatchId : hatchIdList) {
//            workingData.getLogger().logDebug("初始化舱(hatchId:" + hatchId + ")顶层。");
//            if (hatchId == 27753) {
            initCurTopWorkMoveByHatchId(hatchId, moveData, structureData);
//            }
        }
    }

    public void initCurTopWorkMoveByHatchId(Long hatchId, MoveData moveData, StructureData structureData) {
        String dlType;
        VMSlot vmSlot;
        WorkMove workMove;
        moveData.clearCurTopMoveByHatchId(hatchId); //清空该舱的顶层
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, CWPDomain.ROW_SEQ_ODD_EVEN);
        for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
            VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_ABOVE));
            VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_BELOW));
            for (Integer rowNo : rowNoSeqList) {
                dlType = CWPDomain.DL_TYPE_DISC;
                if (vmBayA.getVMRowByRowNo(rowNo) != null) {
                    vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, vmBayA.getVMRowByRowNo(rowNo).getTopTierNo()).getVLocation());
                    workMove = moveData.getWorkMoveByVMSlot(vmSlot, dlType);
                    if (workMove != null && workMove.getMoveOrder() == null) {
                        if (isTopWorkMove(workMove, moveData, structureData)) {
                            moveData.putCurTopWorkMove(hatchId, workMove);
                        }
                        continue;
                    }
                    int bottom = 50;
                    if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null
                            && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                        bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
                    }
                    while (structureData.hasNextVMSlot(vmSlot, dlType)) {
                        if (CWPDomain.DL_TYPE_DISC.equals(dlType) && vmSlot.getVmPosition().getTierNo() == bottom) {
                            dlType = CWPDomain.DL_TYPE_LOAD;
                            workMove = moveData.getWorkMoveByVMSlot(vmSlot, dlType);
                            if (workMove != null && workMove.getMoveOrder() == null) {
                                if (isTopWorkMove(workMove, moveData, structureData)) {
                                    moveData.putCurTopWorkMove(hatchId, workMove);
                                }
                                break;
                            }
                        }
                        vmSlot = structureData.getNextVMSlot(vmSlot, dlType);
                        workMove = moveData.getWorkMoveByVMSlot(vmSlot, dlType);
                        if (workMove != null && workMove.getMoveOrder() == null) {
                            if (isTopWorkMove(workMove, moveData, structureData)) {
                                moveData.putCurTopWorkMove(hatchId, workMove);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isTopWorkMove(WorkMove workMove, MoveData moveData, StructureData structureData) {
        if (workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
            WorkMove workMoveD = moveData.getWorkMoveByVMSlot(workMove.getOneVMSlot(), CWPDomain.DL_TYPE_DISC);
            if (workMoveD != null && workMoveD.getMoveOrder() == null) { //卸船的move没有做， 装船的move就不能做
                return false;
            }
        }
        //该集装箱、舱盖板Move所有VMSlot上面/下面的VMSlot对应的Move是空或者已经编序
        for (VMSlot vmSlot : workMove.getVmSlotSet()) {
            VMSlot vmSlotFront = vmSlot;
            String dlType = workMove.getDlType();
            VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_ABOVE));
            VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_BELOW));
            int bottom = 50;
            if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null
                    && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
            }
            while (structureData.hasFrontVMSlot(vmSlotFront, dlType)) {
                if (CWPDomain.DL_TYPE_LOAD.equals(dlType) && vmSlotFront.getVmPosition().getTierNo() == bottom) { //由装变成卸
                    dlType = CWPDomain.DL_TYPE_DISC;
                    WorkMove workMoveFront = moveData.getWorkMoveByVMSlot(vmSlotFront, dlType);
                    if (workMoveFront != null && workMoveFront.getMoveOrder() == null) {
                        return false;
                    }
                }
                vmSlotFront = structureData.getFrontVMSlot(vmSlotFront, dlType);
                WorkMove workMoveFront = moveData.getWorkMoveByVMSlot(vmSlotFront, dlType);
                if (workMoveFront != null && workMoveFront.getMoveOrder() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<WorkMove> getNextTopWorkMoveSet(WorkMove workMove, MoveData moveData, StructureData structureData) {
        Set<WorkMove> workMoveSet = new LinkedHashSet<>();
        for (VMSlot vmSlot : workMove.getVmSlotSet()) {
            VMSlot vmSlotNext = vmSlot;
            String dlType = workMove.getDlType();
            VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_ABOVE));
            VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_BELOW));
            int bottom = 50;
            if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null
                    && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
            }
            while (structureData.hasNextVMSlot(vmSlotNext, dlType)) {
                if (CWPDomain.DL_TYPE_DISC.equals(dlType) && vmSlotNext.getVmPosition().getTierNo() == bottom) { //从卸船变成装船
                    dlType = CWPDomain.DL_TYPE_LOAD;
                    WorkMove workMoveNext = moveData.getWorkMoveByVMSlot(vmSlotNext, dlType);
                    if (workMoveNext != null && workMoveNext.getMoveOrder() == null) {
                        if (workMoveNext.getBayNo().equals(workMove.getBayNo())) {
                            if (isTopWorkMove(workMoveNext, moveData, structureData)) {
                                workMoveSet.add(workMoveNext);
                            }
                        }
                        break;
                    }
                }
                vmSlotNext = structureData.getNextVMSlot(vmSlotNext, dlType);
                WorkMove workMoveNext = moveData.getWorkMoveByVMSlot(vmSlotNext, dlType);
                if (workMoveNext != null && workMoveNext.getMoveOrder() == null) {
                    if (workMoveNext.getBayNo().equals(workMove.getBayNo())) {
                        if (isTopWorkMove(workMoveNext, moveData, structureData)) {
                            workMoveSet.add(workMoveNext);
                        }
                    }
                    break;
                }
            }
        }

        return workMoveSet;
    }

    private Map<String, WorkMove> copyWorkMoveMap(Map<String, WorkMove> workMoveMap) {
        Map<String, WorkMove> moveMap = new HashMap<>();
        Set<WorkMove> workMoveSet = new HashSet<>(workMoveMap.values());
        for (WorkMove workMove : workMoveSet) {
            WorkMove workMoveCopy = new WorkMove(workMove.getDlType(), workMove.getWorkFlow(), workMove.getWorkTime(), workMove.getMoveType());
            workMoveCopy.setMoveOrder(workMove.getMoveOrder());
            workMoveCopy.setHatchId(workMove.getHatchId());
            workMoveCopy.setPlanStartTime(workMove.getPlanStartTime());
            workMoveCopy.setPlanEndTime(workMove.getPlanEndTime());
            workMoveCopy.setSelectReason(workMove.getSelectReason());
            workMoveCopy.setCraneNo(workMove.getCraneNo());
            workMoveCopy.setBayNo(workMove.getBayNo());
            workMoveCopy.setRowNo(workMove.getRowNo());
            workMoveCopy.setTierNo(workMove.getTierNo());
            workMoveCopy.setHcSeq(workMove.getHcSeq());
            for (VMSlot vmSlot : workMove.getVmSlotSet()) { //VMSlot不需要深复制
                workMoveCopy.getVmSlotSet().add(vmSlot);
            }
            workMoveCopy.setRowNoList(workMove.getRowNoList()); //舱盖板排号不需要深复制
            for (VMSlot vmSlot : workMoveCopy.getVmSlotSet()) {
                moveMap.put(vmSlot.getVmPosition().getVLocation(), workMoveCopy);
            }
        }
        return moveMap;
    }

    MoveData copyMoveData(MoveData moveData) {
        MoveData moveData1 = new MoveData();
        moveData1.setDiscWorkMoveMap(this.copyWorkMoveMap(moveData.getDiscWorkMoveMap()));
        moveData1.setLoadWorkMoveMap(this.copyWorkMoveMap(moveData.getLoadWorkMoveMap()));
        for (Map.Entry<Long, Long> entry : moveData.getCurMoveOrderMap().entrySet()) {
            moveData1.setCurMoveOrder(entry.getKey(), entry.getValue());
        }
        return moveData1;
    }
}
