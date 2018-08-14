package com.cwp3.single.algorithm.move.calculator;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by csw on 2018/6/6.
 * Description:
 */
public class Calculator implements FirstWorkMove {

    /*
     * 1.先卸后装，即不是边装边卸
     * 2.一层一层的卸，一层一层的装
     * 3.优先作业舱盖板
     * 4.作业工艺要连续  在保持作业工艺连续的前提下，垫脚箱→双箱吊→40尺→单吊小箱
     * 5.作业工艺的切换  同一层内切换作业工艺
     * 6.装船时，危险品箱子上面的箱子最后排。而危险品箱子，正常编序。要特别注意危险品箱子叠在一起的情况。
     * 7.装船时，舱上过道2边的箱子，要和舱上的箱子一起装。
     * */
    public WorkMove findFirstWorkMove(Long hatchId, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData, List<Integer> dangerRow, List<WorkMove> delayedMoveList, StringBuffer preWorkFlow) {
        WorkMove workMove = null;
        String dlType = CWPDomain.DL_TYPE_LOAD;  //当前装卸类型
        Integer curTier = null;         //当前作业层
        String curWorkFlow = CWPCraneDomain.CT_HATCH_COVER;      //当前作业工艺
        List<String> workFlowSeqList = new ArrayList<>();//作业工艺顺序
        workFlowSeqList.add(CWPCraneDomain.CT_DUAL40);
        workFlowSeqList.add(CWPCraneDomain.CT_DUAL20);
        workFlowSeqList.add(CWPCraneDomain.CT_SINGLE40);
        workFlowSeqList.add(CWPCraneDomain.CT_SINGLE20);
        workFlowSeqList.add(CWPCraneDomain.CT_HATCH_COVER);

        //1、先确定装卸类型
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            //假设现在是装船，map中有一个move是卸船，那么就是卸船
            WorkMove move = entry.getValue();
            if (move.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
                dlType = CWPDomain.DL_TYPE_DISC;
                break;
            }
        }


        //2.判断是否只剩下延后的箱子,装箱时
        String oddOrEven = null;
        if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_LAND_SEA);
        } else {
            oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_SEA_LAND);
        }
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, oddOrEven);
        boolean delayWorkFlag = true;
        if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            for (Integer rowNo : rowNoMoveMap.keySet()) {
                if (!dangerRow.contains(rowNo)) {
                    delayWorkFlag = false;
                } else {
                    WorkMove move = rowNoMoveMap.get(rowNo);
                    if (!delayedMoveList.contains(move)) {
                        delayWorkFlag = false;
                    }
                }
            }
        }

        //3.确定当前作业层
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
            //装卸分界---------------------------------------------------------------------------------
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
        //4.确定作业工艺
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
                                if(move.getOneVMSlot().getVmPosition().getTierNo().equals(curTier)){
                                    Integer curIndex = workFlowSeqList.indexOf(move.getWorkFlow());
                                    if (curIndex < workFlowSeqList.indexOf(curWorkFlow)) {
                                        curWorkFlow = move.getWorkFlow();
                                    }
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
                            if(move.getOneVMSlot().getVmPosition().getTierNo().equals(curTier)){
                                Integer curIndex = workFlowSeqList.indexOf(move.getWorkFlow());
                                if (curIndex < workFlowSeqList.indexOf(curWorkFlow)) {
                                    curWorkFlow = move.getWorkFlow();
                                }
                            }
                        }
                    }
                }
            }
        }
        //5.根据装卸类型，当前作业层数，当前作业工艺，是否延后发箱，返回Move
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
                        if (container != null && container.getDgCd() != null && !container.getDgCd().equals(CWPDomain.DG_NORMAL)) {
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
