package com.cwp3.single.algorithm.move.calculator.caseCal;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by CarloJones on 2018/7/23.
 */
public class CurWorkFlow {
    private String curWorkFlow;
    private List<String> workFlowSeqList;

    public CurWorkFlow() {
        this.workFlowSeqList = new ArrayList<>();
    }

    public String getCurWorkFlow(Map<Integer, WorkMove> rowNoMoveMap, List<WorkMove> delayedMoveList, String dlType, StringBuffer preWorkFlow, boolean delayWorkFlag, Integer curTier){
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            WorkMove move = entry.getValue();
            if (!delayWorkFlag) {
                if (!delayedMoveList.contains(move)) {
                    if (move.getDlType().equals(dlType)) {
                        if (move.getOneVMSlot().getVmPosition().getTierNo().equals(curTier)) {
                            if (preWorkFlow.toString().equals(move.getWorkFlow())) {
                                curWorkFlow = preWorkFlow.toString();
                                break;
                            } else {
                                Integer curIndex = workFlowSeqList.indexOf(move.getWorkFlow());
                                if (curIndex < workFlowSeqList.indexOf(curWorkFlow)) {
                                    curWorkFlow = move.getWorkFlow();
                                }
                            }
                        }
                    }
                }
            } else {
                if (move.getDlType().equals(dlType)) {
                    if (move.getOneVMSlot().getVmPosition().getTierNo().equals(curTier)) {
                        if (preWorkFlow.toString().equals(move.getWorkFlow())) {
                            curWorkFlow = preWorkFlow.toString();
                            break;
                        } else {
                            Integer curIndex = workFlowSeqList.indexOf(move.getWorkFlow());
                            if (curIndex < workFlowSeqList.indexOf(curWorkFlow)) {
                                curWorkFlow = move.getWorkFlow();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
