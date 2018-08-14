package com.cwp3.single.algorithm.move;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCraneWorkFlow;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.HatchBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.maker.AbstractMaker;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.utils.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2017/9/19.
 * Description:
 */
public class MoveMaker {

    public void makeMove(Long hatchId, WorkingData workingData, StructureData structureData, String dlType) {
        String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
        //得到该舱有多少个倍，分甲板上、下，左、右倍
        List<VMBay> vmBayList = structureData.getVMBayListByHatchId(hatchId);
        List<Integer> rowSeqList;
        for (VMBay vmBay : vmBayList) {
            int maxTier = vmBay.getMaxTier();
            int minTier = vmBay.getMinTier();
            for (int tierNo = maxTier; tierNo >= minTier; ) {
                //根据bayId, 装卸方向，得到该倍位块所有排号
                rowSeqList = structureData.getRowSeqListByOddOrEven(vmBay.getBayId(), oddOrEven);
                for (Integer rowNo : rowSeqList) {
                    VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(vmBay.getBayNo(), rowNo, tierNo).getVLocation());
                    if (vmSlot != null) {
                        VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                        if (vmContainer != null) {
                            if (PublicMethod.hasNoneWorkFlow(vmContainer.getWorkFlow())) { //没编写作业工艺
                                String key = StringUtil.getKey(StringUtil.getKey(hatchId, vmBay.getAboveOrBelow()), dlType);
                                CMCraneWorkFlow cmCraneWorkFlow = workingData.getCMCraneWorkFlowByKey(key);
                                List<AbstractMaker> ptSeqList = PublicMethod.getPTSeqListByCMCraneWorkFlow(cmCraneWorkFlow);
                                for (AbstractMaker maker : ptSeqList) {
                                    if (maker.canDo(vmSlot, dlType, workingData, structureData)) {
                                        vmContainer.setWorkFlow(maker.getWorkFlow());
                                    }
                                }
                                //集装箱Move
                                if (PublicMethod.isSingleWorkFlow(vmContainer.getWorkFlow())) {
                                    long wt = PublicMethod.getCntWorkTime(vmContainer, workingData.getCwpConfig());
                                    if (structureData.isSteppingVMSlot(vmSlot)) {
                                        wt = wt > workingData.getCwpConfig().getSingle20FootPadTime() ? wt : workingData.getCwpConfig().getSingle20FootPadTime();
                                    }
                                    WorkMove workMove = new WorkMove(dlType, vmContainer.getWorkFlow(), wt, CWPDomain.MOVE_TYPE_CNT);
                                    workMove.addVmSlot(vmSlot);
                                    workMove.setHatchId(hatchId);
                                    workMove.setBayNo(new VMPosition(vmContainer.getvLocation()).getBayNo());
                                    workMove.setRowNo(rowNo);
                                    workMove.setTierNo(tierNo);
                                    if (CWPCraneDomain.CT_SINGLE40.equals(vmContainer.getWorkFlow())) {
                                        VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                                        workMove.addVmSlot(vmSlotPair);
                                    }
                                    //move属于哪个档
                                    workMove.setHcSeq(workingData.getHcSeqByWorkMove(hatchId, workMove));
                                    workingData.addWorkMove(workMove);
                                }
                            }
                        }
                    }
                }
                tierNo -= 2;
            }
        }
        //舱盖板move
        String[] dlTypes = new String[]{CWPDomain.DL_TYPE_DISC, CWPDomain.DL_TYPE_LOAD};
        HatchBlock hatchBlock = workingData.getHatchBlockByHatchId(hatchId);
        long workTime = dlType.equals(CWPDomain.DL_TYPE_DISC) ? workingData.getCwpConfig().getHatchCoverTime() : workingData.getCwpConfig().getHatchCoverTime();
//        Map<Integer, List<Integer>> blockMap = dlType.equals(CWPDomain.DL_TYPE_DISC) ? hatchBlock.getAboveBlockMap() : hatchBlock.getBelowBlockMap();
        Map<Integer, List<Integer>> blockMap = hatchBlock.getAboveBlockMap();
        VMHatchCoverSlot vmHatchCoverSlot;
        for (Integer hcSeq : blockMap.keySet()) {
            WorkMove workMove = new WorkMove(dlType, CWPCraneDomain.CT_HATCH_COVER, workTime, CWPDomain.MOVE_TYPE_HC);
            workMove.setRowNoList(blockMap.get(hcSeq));
            //舱盖板下面有箱子有箱子与否，决定舱盖板move是否存在
            boolean has = false;
            BayNo:
            for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                for (Integer rowNo : workMove.getRowNoList()) {
                    for (int tierNo = 50; tierNo >= 0; ) {
                        VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, tierNo).getVLocation());
                        for (String dlType1 : dlTypes) {
                            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType1);
                            if (vmContainer != null) {
                                has = true;
                                break BayNo;
                            }
                        }
                        tierNo -= 2;
                    }
                }
            }
            if (has) {
                //添加vmHatchCoverSlot
                for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                    VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_ABOVE));
                    for (Integer rowNo : workMove.getRowNoList()) {
                        if (vmBayA.getVMRowByRowNo(rowNo) != null) { //船舶结构没有的槽，舱盖板slot不初始化
                            vmHatchCoverSlot = (VMHatchCoverSlot) structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, 50).getVLocation());
                            workMove.addVmSlot(vmHatchCoverSlot);
                            workMove.setRowNo(rowNo);
                        }
                    }
                }
                workMove.setHatchId(hatchId);
                workMove.setBayNo(structureData.getVMHatchByHatchId(hatchId).getBayNoD());
                workMove.setTierNo(50);
                //move属于哪个档
                workMove.setHcSeq(hcSeq);
                workingData.addWorkMove(workMove);
            }
        }
    }

}
