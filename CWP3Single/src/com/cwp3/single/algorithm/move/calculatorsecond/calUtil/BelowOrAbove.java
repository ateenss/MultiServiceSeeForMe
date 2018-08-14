package com.cwp3.single.algorithm.move.calculatorsecond.calUtil;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;

import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/1911:05
 * @Description:
 * 判断装船作业位置是甲板上还是甲板下
 */
public class BelowOrAbove {
    public Map<Integer, WorkMove> rowNoMoveMap;
    public String dlType;
    public Integer hatchCoverFlag;
    public BelowOrAbove(Map<Integer, WorkMove> rowNoMoveMap,String dlType,Integer hatchCoverFlag){
        this.rowNoMoveMap=rowNoMoveMap;
        this.dlType=dlType;
        this.hatchCoverFlag=hatchCoverFlag;
    }
    public String doJudge(){
            String result=null;
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                WorkMove move = entry.getValue();
                if(move.getDlType().equals(CWPDomain.DL_TYPE_LOAD)
                        && move.getTierNo()<= hatchCoverFlag){
                    result=  CWPDomain.BOARD_BELOW;
                }else{
                    result= CWPDomain.BOARD_ABOVE;
                }
            }
            return result;
    }
}
