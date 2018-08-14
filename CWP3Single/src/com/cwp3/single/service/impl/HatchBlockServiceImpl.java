package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMHatchCover;
import com.cwp3.model.vessel.VMHatchCoverSlot;
import com.cwp3.model.vessel.VMPosition;
import com.cwp3.model.work.HatchBlock;
import com.cwp3.single.service.HatchBlockService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class HatchBlockServiceImpl implements HatchBlockService {

    @Override
    public void makeHatchBlock(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        StructureData structureData = allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode());
        List<Long> hatchIdList = structureData.getAllHatchIdList();
        workingData.getLogger().logInfo("调用分档算法，对船舶(berthId:" + berthId + ")进行分档处理。");
        for (Long hatchId : hatchIdList) {
            makeHatchBlockByHatchId(hatchId, workingData, structureData);
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")分档结束。");
    }

    private void makeHatchBlockByHatchId(Long hatchId, WorkingData workingData, StructureData structureData) {
//        workingData.getLogger().logDebug("对舱(hatchId:" + hatchId + ")进行分档处理。");
        try {
            List<VMHatchCover> vmHatchCoverList = structureData.getHatchCoverListByHatchId(hatchId);
            String oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_LAND_SEA);
            List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, oddOrEven);
            List<Integer> rowNoSeqBelowList = structureData.getRowSeqListBySeaOrLand(hatchId, CWPDomain.BOARD_BELOW, oddOrEven);
            HatchBlock hatchBlock = new HatchBlock(hatchId);
            boolean oneBlock = false;
            if (vmHatchCoverList.size() == 0) {
                oneBlock = true;
            }
            for (VMHatchCover vmHatchCover : vmHatchCoverList) {
                int as = -1, ae = -1, bs = -1, be = -1;
                for (int j = 0; j < rowNoSeqList.size(); j++) {
                    if (vmHatchCover.getDeckFromRowNo().equals(rowNoSeqList.get(j))) {
                        as = j;
                    }
                    if (vmHatchCover.getDeckToRowNo().equals(rowNoSeqList.get(j))) {
                        ae = j;
                    }
                    if (vmHatchCover.getHatchFromRowNo().equals(rowNoSeqList.get(j))) {
                        bs = j;
                    }
                    if (vmHatchCover.getHatchToRowNo().equals(rowNoSeqList.get(j))) {
                        be = j;
                    }
                }
                List<Integer> aboveRowNoList = new ArrayList<>();
                List<Integer> belowRowNoList = new ArrayList<>();
                if (as > -1 && ae > -1 && bs > -1 && be > -1) {
                    addRowNoList(as, ae, aboveRowNoList, rowNoSeqList);
                    addRowNoList(bs, be, belowRowNoList, rowNoSeqList);
                    hatchBlock.addAboveBlock(vmHatchCover.getHatchCoverNo(), aboveRowNoList);
                    hatchBlock.addBelowBlock(vmHatchCover.getHatchCoverNo(), belowRowNoList);
                    hatchBlock.addVMHatchCover(vmHatchCover.getHatchCoverNo(), vmHatchCover);
                } else {
                    oneBlock = true;
                    workingData.getLogger().logWarn("舱盖板(hatchCoverId:" + vmHatchCover.getHatchCoverNo() + ")起止排号异常，整舱当作没有舱盖板处理");
                    break;
                }
            }
            /*将过道包含到block中*/
            List<Integer> aisleList = new ArrayList<>(rowNoSeqList);
            for (Integer i : hatchBlock.getAboveBlockMap().keySet()) {
                aisleList.removeAll(hatchBlock.getAboveBlockMap().get(i));
            }
            /**
             * 含有过道
             * */
            if (aisleList.size() > 0) {
                Collections.sort(aisleList);
                for (Integer i : aisleList) {
                    Integer x = (i - ((aisleList.indexOf(i) / 2) + 1) * 2);
                    for (Integer j : hatchBlock.getAboveBlockMap().keySet()) {
                        List<Integer> targetList = hatchBlock.getAboveBlockMap().get(j);
                        if (targetList.contains(x)) {
                            Integer index = targetList.indexOf(x);
                            if (index > 0) {
                                hatchBlock.getAboveBlockMap().get(j).add(i);
                            } else if (index == 0) {
                                hatchBlock.getAboveBlockMap().get(j).add(0, i);
                            }
                        }
                    }
                }
            }
            if (oneBlock) {
                hatchBlock.addAboveBlock(1, rowNoSeqList);
//                hatchBlock.addAboveBlock(1, rowNoSeqBelowList);
                hatchBlock.addBelowBlock(1, rowNoSeqBelowList);
                VMPosition vmPosition = new VMPosition(structureData.getVMHatchByHatchId(hatchId).getBayNoD(), 1, 50);
                VMHatchCover vmHatchCover = new VMHatchCover(vmPosition.getVLocation(), hatchId);
                hatchBlock.addVMHatchCover(1, vmHatchCover);
            }
            //同时将VMHatchCoverSlot放到StructureData中
            VMHatchCoverSlot vmHatchCoverSlot;
            for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                for (Integer rowNo : rowNoSeqList) {
                    vmHatchCoverSlot = new VMHatchCoverSlot(new VMPosition(bayNo, rowNo, 50), hatchId);
                    structureData.addVMSlot(vmHatchCoverSlot);
                }
            }
            workingData.addHatchBlock(hatchBlock);
        } catch (Exception e) {
            workingData.getLogger().logError("在对舱(" + hatchId + ")进行分档过程中发生异常！");
            e.printStackTrace();
        }
    }

    private void addRowNoList(int s, int e, List<Integer> rowNoList, List<Integer> rowNoSeqList) {
        if (s > e) {
            for (int j = s; j >= e; j--) {
                rowNoList.add(rowNoSeqList.get(j));
            }
        } else {
            for (int j = s; j <= e; j++) {
                rowNoList.add(rowNoSeqList.get(j));
            }
        }
    }
}
