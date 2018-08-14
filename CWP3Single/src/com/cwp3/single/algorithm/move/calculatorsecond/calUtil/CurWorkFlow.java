package com.cwp3.single.algorithm.move.calculatorsecond.calUtil;

import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/19 21:07
 * @Description:
 * 用来获得作业工艺
 */
public class CurWorkFlow {
    private Map<Integer, WorkMove> rowNoMoveMap;
    private String dlType;
    private StringBuffer preWorkFlow;
    private List<String> workFlowSeqList;
    private Integer curTier;
    private String curWorkFlow;
    private List<WorkMove> delayedMoveList;

    public CurWorkFlow(Map<Integer, WorkMove> rowNoMoveMap, String dlType, StringBuffer preWorkFlow,
                       List<String> workFlowSeqList,Integer curTier,String curWorkFlow,List<WorkMove> delayedMoveList) {
        this.rowNoMoveMap = rowNoMoveMap;
        this.dlType = dlType;
        this.preWorkFlow = preWorkFlow;
        this.workFlowSeqList = workFlowSeqList;
        this.curTier=curTier;
        this.curWorkFlow=curWorkFlow;
        this.delayedMoveList=delayedMoveList;
    }


    public String SelectAndGetWorkFlow(){
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            WorkMove move = entry.getValue();
            if (!delayedMoveList.contains(move)) {
                if (move.getDlType().equals(dlType)) {
                    if (move.getOneVMSlot().getVmPosition().getTierNo().equals(curTier)) {
                        //保持作业工艺的连续性
                        if (preWorkFlow.toString().equals(move.getWorkFlow())) {
                            curWorkFlow = preWorkFlow.toString();
                            break;
                        } else {
                            Integer curIndex = workFlowSeqList.indexOf(move.getWorkFlow());
                            if (curIndex < workFlowSeqList.indexOf(curWorkFlow)) {
                            /*判断workFlowSeqList里面的优先级，垫脚箱作为一种
                            单个20尺箱子，最优先吊装，其实大部分单个20尺箱子都是最优先吊装的*/
                                curWorkFlow = move.getWorkFlow();
                            /*这里面有个问题就是，如果发完垫脚箱之后发大箱，最后再发双箱吊小箱，
                            那这个优先级顺序就不好用了*/
                            }
                        }
                    }
                }
            }
        }
        return curWorkFlow;
    }
}
