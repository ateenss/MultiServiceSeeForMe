package com.cwp3.single.algorithm.move.calculatorsecond;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.calculator.FirstWorkMove;
import com.cwp3.single.algorithm.move.calculatorsecond.calUtil.*;
import com.cwp3.single.algorithm.move.calculatorsecond.caseCal.BasicSendContainer;
import com.cwp3.single.algorithm.move.calculatorsecond.caseCal.DangerDelay;
import com.cwp3.single.data.MoveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dongyuhang
 * @date 2018/7/20 9:17
 * @Description:
 * 新写的类，将功能拆分成多个模块，方便调试
 */
public class CalculatorNew implements FirstWorkMove {
    @Override
    public WorkMove findFirstWorkMove(Long hatchId, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData,
                                      WorkingData workingData, StructureData structureData, List<Integer> dangerRow,
                                      List<WorkMove> delayedMoveList, StringBuffer preWorkFlow){


        //因为rowNoMoveMap始终是最顶层可用的箱子，所以每次把它拿出来扫描，总是能拿到想要的条件的
        //要返回的wokmove设置成全局变量，方便后面函数返回调用
        WorkMove workMove =null;
        //当前装卸类型，用来进行内部判断
        String dlType = CWPDomain.DL_TYPE_LOAD;
        //当前作业层，用来进行内部判断
        Integer curTier = null;
        //当前作业工艺，用来进行内部判断，默认初始值被设定为40尺箱
        String curWorkFlow = CWPCraneDomain.CT_DUAL40;
        //按作业工艺发箱：舱盖→垫脚箱→双箱吊→40尺→双吊具。
        List<String> workFlowSeqList = new ArrayList<>();
        workFlowSeqList.add(CWPCraneDomain.CT_HATCH_COVER);
        workFlowSeqList.add(CWPCraneDomain.CT_SINGLE20);
        //考虑垫脚箱，从而改变list中排序
        workFlowSeqList.add(CWPCraneDomain.CT_DUAL20);
        workFlowSeqList.add(CWPCraneDomain.CT_SINGLE40);
        workFlowSeqList.add(CWPCraneDomain.CT_DUAL40);
        //判断是甲板上作业，还是甲板下作业
        String aboveOrBelow =CWPDomain.BOARD_ABOVE;
        //判断是否因为危险品而延后的开关，本类中遭到废弃
//        boolean delayWorkFlag=true;
        //舱内最高层
        Integer hatchCoverFlag=new Gettopest(hatchId,structureData).getTopTierInHatch();
        //第一步，先来确定装卸类型
        LoadOrDisc b=new LoadOrDisc(rowNoMoveMap,dlType);
        dlType=b.doJudge();
        //判断是否是仓盖板，如果是舱盖板的话，提前返回
        if (dlType.equals(CWPDomain.MOVE_TYPE_HC)){
            return b.getResult();
        }
        //根据装卸情况，判断排号的顺序
        List<Integer> rowNoSeqList=new GetRowNumList(dlType,workingData, hatchId, structureData).getRowNumList();
        //判断危险品
        DangerDelay it =new DangerDelay();
        it.getDangerDelay( dangerRow,delayedMoveList,rowNoMoveMap,dlType,rowNoSeqList,workingData);
        //第二步，再来确定是在甲板上还是甲板下，方便后面判断,注意在使用的过程中还需要判断是装船的情况
        aboveOrBelow=new BelowOrAbove(rowNoMoveMap,dlType,hatchCoverFlag).doJudge();
        //第三步，确定当前摆放层
        curTier=new CurWorkTier(dlType,rowNoMoveMap, curTier, preWorkFlow,aboveOrBelow,hatchCoverFlag,delayedMoveList).getCurTier();
        //第四步，确定作业工艺
        curWorkFlow=new CurWorkFlow(rowNoMoveMap, dlType, preWorkFlow,workFlowSeqList,curTier, curWorkFlow,delayedMoveList).SelectAndGetWorkFlow();
        //第五步，返回workmove，并对preworkflow做修改
        workMove=new BasicSendContainer(dlType,rowNoMoveMap,preWorkFlow, curTier,curWorkFlow,rowNoSeqList,delayedMoveList).judgeAndGetMove();
        //最后返回workmove
        return workMove;
    }

}
