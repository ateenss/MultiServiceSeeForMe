package com.cwp3.single.algorithm.move.calculator;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.List;
import java.util.Map;

/**
 * Created by CarloJones on 2018/6/12.
 */
public interface FirstWorkMove {
    public WorkMove findFirstWorkMove(Long hatchId, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData, List<Integer> dangerRow, List<WorkMove> delayedMoveList,StringBuffer preWorkFlow);
}
