package com.cwp3.single.algorithm.move.calculator.calUtil;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * Created by CarloJones on 2018/7/23.
 */
public class DangerDelay {
    List<Integer> dangerRow;
    List<WorkMove> delayedMoveList;

    public DangerDelay(List<Integer> dangerRow, List<WorkMove> delayedMoveList) {
        this.dangerRow = dangerRow;
        this.delayedMoveList = delayedMoveList;
    }

    public boolean isDelayContainerOnly(Long hatchId, Map<Integer, WorkMove> rowNoMoveMap, WorkingData workingData, StructureData structureData, String dlType){
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
        return delayWorkFlag;
    }


}
