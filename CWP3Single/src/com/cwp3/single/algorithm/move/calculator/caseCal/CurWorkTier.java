package com.cwp3.single.algorithm.move.calculator.caseCal;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * Created by CarloJones on 2018/7/23.
 */
public class CurWorkTier {
    private Integer curTier;
    private String preWorkFlow;
    private boolean delayWorkFlag;
    private List<WorkMove> delayedMoveList;

    public CurWorkTier(Integer curTier, String preWorkFlow, boolean delayWorkFlag, List<WorkMove> delayedMoveList) {
        this.curTier = curTier;
        this.preWorkFlow = preWorkFlow;
        this.delayWorkFlag = delayWorkFlag;
        this.delayedMoveList = delayedMoveList;
    }

    private Integer getCurWorkTier(Long hatchId, Map<Integer, WorkMove> rowNoMoveMap, WorkingData workingData, StructureData structureData, String dlType){
        if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                WorkMove move = entry.getValue();
                if (move.getDlType().equals(dlType)) {
                    Integer tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                    if ((move.getWorkFlow().equals(preWorkFlow.toString()) || preWorkFlow.toString().equals(""))
                            && curTier == null) {
                        curTier = tierNo;
                    } else if ((move.getWorkFlow().equals(preWorkFlow.toString())||preWorkFlow.toString().equals("")) && tierNo > curTier) {
                        curTier = tierNo;
                    }
                }
            }
            //作业工艺不能够延续下来
            if (curTier == null) {
                for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                    WorkMove move = entry.getValue();
                    if (move.getDlType().equals(dlType)) {
                        Integer tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                        if (curTier == null) {
                            curTier = tierNo;
                        } else if (tierNo > curTier) {
                            curTier = tierNo;
                        }
                    }
                }
            }
        } else if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                WorkMove move = entry.getValue();
                if (!delayWorkFlag) {
                    if (!delayedMoveList.contains(move)) {
                        if (move.getDlType().equals(dlType)) {
                            Integer tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                            //本循环第一次执行
                            if ((move.getWorkFlow().equals(preWorkFlow.toString()) || preWorkFlow.toString().equals(""))
                                    && curTier == null) {
                                curTier = tierNo;
                                //本循环执行多次了
                            } else if((move.getWorkFlow().equals(preWorkFlow.toString())||preWorkFlow.toString().equals(""))) {
                                if(tierNo < curTier){
                                    curTier = tierNo;
                                }
                            }
                        }
                    }
                } else {
                    if (move.getDlType().equals(dlType)) {
                        Integer tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                        //本循环第一次执行
                        if ((move.getWorkFlow().equals(preWorkFlow.toString()) || preWorkFlow.toString().equals(""))
                                && curTier == null) {
                            curTier = tierNo;
                            //本循环执行多次了
                            if(tierNo < curTier){
                                curTier = tierNo;
                            }
                        }
                    }
                }
            }
            //作业工艺不能够连续的情况下，即不考虑preWorkFlow
            if (curTier == null) {
                for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                    WorkMove move = entry.getValue();
                    if (!delayWorkFlag) {
                        if (!delayedMoveList.contains(move)) {
                            if (move.getDlType().equals(dlType)) {
                                Integer tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                                if (curTier == null) {
                                    curTier = tierNo;
                                } else {
                                    if(tierNo < curTier){
                                        curTier = tierNo;
                                    }
                                }
                            }
                        }
                    } else {
                        if (move.getDlType().equals(dlType)) {
                            Integer tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                            if (curTier == null) {
                                curTier = tierNo;
                            } else {
                                if(tierNo < curTier){
                                    curTier = tierNo;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
