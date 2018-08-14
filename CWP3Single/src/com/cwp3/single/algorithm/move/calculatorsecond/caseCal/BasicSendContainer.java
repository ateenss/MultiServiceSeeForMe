package com.cwp3.single.algorithm.move.calculatorsecond.caseCal;

import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/19 13:52
 * @Description:
 * 描述的基础装箱操作
 * 1)	从海侧往陆侧按层进行发箱
 * 2)	按作业工艺发箱：垫脚箱→双箱吊→40尺→单吊小箱。
 * 3）  按多个条件判断返回workmove

 */
public class BasicSendContainer {
    private String dlType;
    private Map<Integer, WorkMove> rowNoMoveMap;
    private StringBuffer preWorkFlow;
    private String curWorkFlow;
    private  Integer curTier;
    private List<Integer> rowNoSeqList;
    private List<WorkMove> delayedMoveList;
    public BasicSendContainer(String dlType, Map<Integer, WorkMove> rowNoMoveMap,StringBuffer preWorkFlow, Integer curTier,
                              String curWorkFlow,List<Integer> rowNoSeqList,List<WorkMove> delayedMoveList) {
        this.dlType=dlType;
        this.rowNoMoveMap=rowNoMoveMap;
        this.preWorkFlow=preWorkFlow;
        this.curTier=curTier;
        this.curWorkFlow=curWorkFlow;
        this.rowNoSeqList=rowNoSeqList;
        this.delayedMoveList=delayedMoveList;
    }

    public  WorkMove judgeAndGetMove(){
        //根据选好的条件判断筛选，返回相应的WorkMove
        WorkMove workMove=null;
        for (Integer rowNo : rowNoSeqList) {
            WorkMove move = rowNoMoveMap.get(rowNo);
            if (    !delayedMoveList.contains(move)
                    && move != null
                    && move.getDlType().equals(dlType)
                    && move.getVmSlotSet().iterator().next().getVmPosition().getTierNo().equals(curTier)
                    && move.getWorkFlow().equals(curWorkFlow))
                {
                workMove = move;
                preWorkFlow.setLength(0);
                preWorkFlow.append(move.getWorkFlow());
                break;
            }
        }
        return workMove;
    }



}
