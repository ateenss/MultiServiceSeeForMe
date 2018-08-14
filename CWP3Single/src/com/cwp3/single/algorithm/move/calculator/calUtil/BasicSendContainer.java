package com.cwp3.single.algorithm.move.calculator.calUtil;

import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * Created by CarloJones on 2018/7/23.
 */
public class BasicSendContainer {
    private WorkMove workMove;
    private List<Integer> dangerRow;
    private String curWorkFlow;
    private List<Integer> rowNoSeqList;

    public BasicSendContainer(List<Integer> rowNoSeqList) {
        this.rowNoSeqList = rowNoSeqList;
    }

    public WorkMove sendContainer(WorkingData workingData, Map<Integer, WorkMove> rowNoMoveMap, List<WorkMove> delayedMoveList, String dlType, StringBuffer preWorkFlow, boolean delayWorkFlag, Integer curTier){
        for (Integer rowNo : rowNoSeqList) {
            WorkMove move = rowNoMoveMap.get(rowNo);
            boolean delayFlag = false; //用来跳过延后的箱子
            if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
                if (!delayWorkFlag) {
                    //将因为危险品箱而延后的箱子，记录到delayedMoveList中
                    if (move != null && dangerRow.contains(rowNo) && move.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
                        for (VMSlot slot : move.getVmSlotSet()) {
                            VMContainer container = workingData.getVMContainerByVMSlot(slot, CWPDomain.DL_TYPE_LOAD);
                            //连续堆叠的危险品箱，可以正常发箱。
                            if (container.getDgCd() == null || container.getDgCd().equals(CWPDomain.DG_NORMAL)) {
                                if (!delayedMoveList.contains(move)) {
                                    delayedMoveList.add(move);
                                    delayFlag = true;
                                } else {
                                    delayFlag = true;
                                }
                            }
                        }
                        //若move是被延后的箱子，则跳过该move
                        if (delayFlag) {
                            continue;
                        }
                    }
                }
                //判断move中有没有危险品箱，如果move中有危险品，将该row添加到dangerRow中去
                if (move != null && move.getDlType().equals(CWPDomain.DL_TYPE_LOAD)){
                    for (VMSlot slot : move.getVmSlotSet()) {
                        VMContainer container = workingData.getVMContainerByVMSlot(slot, CWPDomain.DL_TYPE_LOAD);
                        if (container.getDgCd() != null && !container.getDgCd().equals(CWPDomain.DG_NORMAL)) {
                            if (!dangerRow.contains(rowNo)) {
                                dangerRow.add(rowNo);
                            }
                        }
                    }
                }
            }
            //返回WorkMove对象
            if (!delayFlag && move != null
                    && move.getDlType().equals(dlType)
                    && move.getVmSlotSet().iterator().next().getVmPosition().getTierNo().equals(curTier)
                    && move.getWorkFlow().equals(curWorkFlow)) {
                workMove = move;
                preWorkFlow.setLength(0);
                preWorkFlow.append(move.getWorkFlow());
                break;
            }
        }
        return workMove;
    }
}
