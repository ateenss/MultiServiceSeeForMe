package com.cwp3.single.algorithm.move.calculatorsecond.caseCal;

import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/24 10:04
 * @Description:
 * 危险品滞后操作类
 */
public class DangerDelay {


    public void getDangerDelay(List<Integer> dangerRow, List<WorkMove> delayedMoveList, Map<Integer, WorkMove> rowNoMoveMap,
                       String dlType,List<Integer> rowNoSeqList,WorkingData workingData) {

//具体研究一下危险品情况


   /* public boolean doJudge(){
        if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            for (Integer rowNo : rowNoMoveMap.keySet()) {
               *//* 这样玩循环的话，如果在有危险箱的基础上进行扫描，那么一定会出现，
                delayWorkFlag变成true的情况，如果用这种办法，就是循环一次，无法控制delayworkflag的值变化情况*//*
                if (!dangerRow.contains(rowNo)) {
                    delayWorkFlag = false;
                    //先去找危险排，如果在危险排中，则继续找延后发箱列表里面是否含有该动作延后发箱的情况
                } else {
                    WorkMove move = rowNoMoveMap.get(rowNo);
                    if (!delayedMoveList.contains(move)) {
                        delayWorkFlag = false;
                    }
                }
            }
        }
        return delayWorkFlag;

    }*/


        for (Integer rowNo : rowNoSeqList) {
            WorkMove move = rowNoMoveMap.get(rowNo);
            //用来跳过延后的箱子,就是节省循环判断
            if (dlType.equals(CWPDomain.DL_TYPE_LOAD)){
                    //将因为危险品箱而延后的箱子，记录到delayedMoveList中
                    if (move != null && dangerRow.contains(rowNo) && move.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
                        //先检测是不是有危险品排，如果是危险品排的话，就进行记录操作
                        for (VMSlot slot : move.getVmSlotSet()) {
                            //利用一个动作，得到一个slot位置，再根据slot位置得出集装箱,然后才能根据集装箱去推断是否是危险箱子
                            VMContainer container = workingData.getVMContainerByVMSlot(slot, CWPDomain.DL_TYPE_LOAD);
                            //连续堆叠的危险品箱，可以正常发箱。
                            if (container.getDgCd() == null || container.getDgCd().equals(CWPDomain.DG_NORMAL)) {
                                //这种情况发现是正常箱子，然后要把符合危险排的move放入到滞后列表里面
                                if (!delayedMoveList.contains(move)) {
                                    delayedMoveList.add(move);
                                    continue;
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                //判断move中有没有危险品箱，如果move中有危险品，将该row添加到dangerRow中去
                //专门用来判断是不是危险品箱的，然后是危险品箱，正常装，但是要标记哪些排是危险排
                if (move != null && move.getDlType().equals(CWPDomain.DL_TYPE_LOAD)){
                    for (VMSlot slot : move.getVmSlotSet()) {
                        VMContainer container = workingData.getVMContainerByVMSlot(slot, CWPDomain.DL_TYPE_LOAD);
                        if (container.getDgCd() != null && !container.getDgCd().equals(CWPDomain.DG_NORMAL)) {
                            if (!dangerRow.contains(rowNo)) {
                                //主要做的就是在本函数中修改dangerROW
                                dangerRow.add(rowNo);
                            }
                        }
                    }
                }
            }
        }
    }


}
