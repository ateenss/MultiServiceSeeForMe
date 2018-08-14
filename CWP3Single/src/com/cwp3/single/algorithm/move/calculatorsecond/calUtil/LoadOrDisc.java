package com.cwp3.single.algorithm.move.calculatorsecond.calUtil;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;

import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/1910:41
 * @Description:
 * 确定装卸类型
 * 由于在主调函数里面默认的装卸类型是装货，如果在这里判断不是舱盖板和卸货的话，自动跳出，仍然引用默认的装货类型
 */
public class LoadOrDisc {
    public Map<Integer, WorkMove> rowNoMoveMap;
    public String dlType;
    public WorkMove workMove;
    public LoadOrDisc(Map<Integer, WorkMove> rowNoMoveMap,String dlType){
        this.rowNoMoveMap=rowNoMoveMap;
        this.dlType=dlType;
    }
    public String doJudge(){
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            //优先处理舱盖板
            //假设现在是装船，map中有一个move是卸船，那么就是卸船
            WorkMove move = entry.getValue();
            if (move.getMoveType().equals(CWPDomain.MOVE_TYPE_HC)) {
                dlType=move.getMoveType();
                workMove=move;
                break;
            } else if (move.getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                if (move.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
                    //只要有一个卸货那就是卸货
                    dlType = CWPDomain.DL_TYPE_DISC;
                    break;
                }
            }
        }
        return dlType;
    }
    public WorkMove getResult(){
        return workMove;
    }
}
