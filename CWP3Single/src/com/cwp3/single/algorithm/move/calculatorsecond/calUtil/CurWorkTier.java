package com.cwp3.single.algorithm.move.calculatorsecond.calUtil;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;

import java.util.List;
import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/1913:44
 * @Description:
 * 确定当前作业层
 */
public class CurWorkTier {
    private String dlType;
    private Map<Integer, WorkMove> rowNoMoveMap;
    private Integer curTier;
    private StringBuffer preWorkFlow;
    private String aboveOrBelow;
    private Integer hatchCoverFlag;
    private Integer tierNo=null;
    private WorkMove move;
    private List<WorkMove> delayedMoveList;
    //下面是本类中方法控制功能区
    private Boolean flag1 = Boolean.FALSE;
    private Boolean flag2 = Boolean.FALSE;


    public CurWorkTier(String dlType, Map<Integer, WorkMove> rowNoMoveMap, Integer curTier, StringBuffer preWorkFlow,
                       String aboveOrBelow, Integer hatchCoverFlag,List<WorkMove> delayedMoveList) {
        this.dlType = dlType;
        this.rowNoMoveMap = rowNoMoveMap;
        this.curTier = curTier;
        this.preWorkFlow = preWorkFlow;
        this.aboveOrBelow = aboveOrBelow;
        this.hatchCoverFlag = hatchCoverFlag;
        this.delayedMoveList=delayedMoveList;
    }

    public Integer getCurTier() {
        //以下为卸载装载一起考虑了，所以通过控制开关函数，控制卸载和装载两种情况，判断第一次做还是循环多次做
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            move = entry.getValue();
            if (!delayedMoveList.contains(move)) {
                if (move.getDlType().equals(dlType)) {
                    tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                    //控制开关函数
                    control();
                    //保持和前一个作业工艺的一致性
                    if (move.getWorkFlow().equals(preWorkFlow.toString()) || preWorkFlow.toString().equals("")) {
                        if (curTier == null) {
                            //第一次判断
                            if (flag2) {
                                curTier = tierNo;
                            } else {
                                curTier = tierNo;
                            }
                        } else {
                            //循环多次
                            if (flag2 && flag1) {
                                curTier = tierNo;
                            } else if (flag1) {
                                curTier = tierNo;
                            }
                        }
                    }
                }
            }
            flag1 = Boolean.FALSE;
            flag2 = Boolean.FALSE;
        }
        //此为作业工艺不连续
        if(curTier == null) {
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                move = entry.getValue();
                if (!delayedMoveList.contains(move)){
                    if (move.getDlType().equals(dlType)) {
                        tierNo = move.getVmSlotSet().iterator().next().getVmPosition().getTierNo();
                        if (curTier == null) {
                            curTier = tierNo;
                        } else if (flag1) {
                            curTier = tierNo;
                        }
                    }
            }
                flag1 = Boolean.FALSE;
            }
        }
        return curTier;
    }


    public void control(){
        //以装卸船为分界点，设置开关控制组功能
        if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {

            //此处按照原方法只考虑了装船时在舱内装的情况
            if (aboveOrBelow.equals(CWPDomain.BOARD_BELOW) && tierNo <= hatchCoverFlag) {
                flag2 = Boolean.TRUE;
            }
            if(curTier==null){
                flag1 = Boolean.TRUE;
            } else if (tierNo < curTier) {
                //curTier第一次运算的时候是null，是无法比较的
                flag1 = Boolean.TRUE;
                //装船的时候找最小
            }
        } else {
            flag2 = Boolean.TRUE;
            if(curTier==null){
                flag1 = Boolean.TRUE;
            } else if (tierNo > curTier) {
                flag1 = Boolean.TRUE;
            }
        }
    }
}
