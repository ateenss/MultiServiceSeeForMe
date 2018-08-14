package com.cwp3.single.algorithm.move;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.calculator.Calculator;
import com.cwp3.single.algorithm.move.calculator.FirstWorkMove;
import com.cwp3.single.data.MoveData;
import com.cwp3.single.data.MoveResults;
import com.cwp3.single.method.MoveDataMethod;

import java.util.*;

/**
 * Created by csw on 2018/5/31.
 * Description:
 */
public class MoveCalculator {

    public void calculateAvailableMove(Long hatchId, MoveData moveData, MoveResults moveResults, WorkingData workingData, StructureData structureData) {
        List<Integer> bayNos = structureData.getVMHatchByHatchId(hatchId).getAllBayNos();
        FirstWorkMove calculator = new Calculator();
        MoveDataMethod moveDataMethod = new MoveDataMethod();
        for (Integer bayNo : bayNos) { //按倍位计算可作业量
            moveResults.clearAvailableMoveByBayNo(bayNo);
            long order = moveData.getCurMoveOrder(hatchId);
            List<WorkMove> workMoveList = moveData.getCurTopMoveListByHatchIdAndBayNo(hatchId, bayNo);
            Map<Integer, WorkMove> rowNoMoveMap = curTopMoveListToMap(workMoveList); //顶层Map，主要对这个Map进行遍历查找最合适作业的move
            List<Integer> dangerRow = new ArrayList<>();//危险品所在row
            List<WorkMove> delayedMoveList = new ArrayList<>();//因危险品而延后装载的move
            StringBuffer preWorkFlow = new StringBuffer(moveData.getCurWorkFlow(hatchId) != null ? moveData.getCurWorkFlow(hatchId) : "");
            List<WorkMove> workMoveFirstList = new ArrayList<>();
            while (rowNoMoveMap.size() > 0) {
                WorkMove workMoveFirst = calculator.findFirstWorkMove(hatchId, rowNoMoveMap, moveData, workingData, structureData, dangerRow, delayedMoveList, preWorkFlow);
                if (workMoveFirst != null) {
                    //作为可作业量进行编序
                    workMoveFirst.setMoveOrder(order++);
                    workMoveFirstList.add(workMoveFirst);
                    //深复制move，不然对其它倍位的判断有问题
                    WorkMove workMoveFirstCopy = workMoveFirst.baseCopy();
                    workMoveFirstCopy.setMoveOrder(workMoveFirst.getMoveOrder());
                    Integer hcSeq = workingData.getHcSeqByWorkMove(hatchId, workMoveFirstCopy); //分档序号
                    moveResults.addAvailableMove(bayNo, hcSeq, workMoveFirstCopy);
                    rowNoMoveMap.remove(workMoveFirst.getRowNo()); //从顶层Map中去掉计算过的move
                    Set<WorkMove> workMoveNextSet = moveDataMethod.getNextTopWorkMoveSet(workMoveFirst, moveData, structureData);
                    for (WorkMove workMoveNext : workMoveNextSet) {
                        rowNoMoveMap.put(workMoveNext.getRowNo(), workMoveNext); //将下一个顶层放到顶层Map中
                    }
                }
            }
            for (WorkMove workMove : workMoveFirstList) {
                workMove.setMoveOrder(null);
            }
        }
    }

    private Map<Integer, WorkMove> curTopMoveListToMap(List<WorkMove> workMoveList) {
        Map<Integer, WorkMove> rowNoMoveMap = new LinkedHashMap<>();
        for (WorkMove workMove : workMoveList) {
            rowNoMoveMap.put(workMove.getRowNo(), workMove);
        }
        return rowNoMoveMap;
    }

    public void calculateTotalMove(Long hatchId, MoveData moveData, MoveResults moveResults, StructureData structureData) {
        for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getAllBayNos()) {
            moveResults.clearCurrentTotalMoveByBayNo(bayNo);
            List<WorkMove> workMoveList = moveData.getTotalMoveListByBayNo(bayNo);
            Set<WorkMove> workMoveSet = new HashSet<>(workMoveList);
            for (WorkMove workMove : workMoveSet) {
                moveResults.addCurrentTotalMove(bayNo, workMove.getHcSeq(), workMove);
            }
        }
    }
}
