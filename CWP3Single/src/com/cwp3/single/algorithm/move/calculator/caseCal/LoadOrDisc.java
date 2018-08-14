package com.cwp3.single.algorithm.move.calculator.caseCal;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.List;
import java.util.Map;

/**
 * Created by CarloJones on 2018/7/23.
 */
public class LoadOrDisc {
    //todo Line25处理舱盖板
    public String confirmDLTyple(Map<Integer, WorkMove> rowNoMoveMap) {
        String dlType = CWPDomain.DL_TYPE_LOAD;  //当前装卸类型
        //1、先确定装卸类型
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            //优先处理舱盖板
            //假设现在是装船，map中有一个move是卸船，那么就是卸船
            WorkMove move = entry.getValue();
            if (move.getMoveType().equals(CWPDomain.MOVE_TYPE_HC)) {
//                return move;
            } else if (move.getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                if (move.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
                    dlType = CWPDomain.DL_TYPE_DISC;
                    break;
                }
            }
        }
        return dlType;
    }
}
