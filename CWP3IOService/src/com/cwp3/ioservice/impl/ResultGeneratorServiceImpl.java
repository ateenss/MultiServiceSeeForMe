package com.cwp3.ioservice.impl;

import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.ioservice.ResultGeneratorService;
import com.cwp3.ioservice.method.CraneOrderMethod;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.CraneEfficiency;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.single.data.MoveData;
import com.cwp3.utils.BeanCopyUtil;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpBlockInfo;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpCraneEfficiencyInfo;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpModalInfo;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpWorkOrderInfo;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;

import java.util.*;

/**
 * Created by csw on 2018/6/7.
 * Description:
 */
public class ResultGeneratorServiceImpl implements ResultGeneratorService {

    @Override
    public void generateCwpResult(MoveData moveData, WorkingData workingData) {
        for (Map.Entry<String, WorkMove> entry : moveData.getDiscWorkMoveMap().entrySet()) {
            for (VMSlot vmSlot : entry.getValue().getVmSlotSet()) {
                if (entry.getValue().getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    workingData.getVMContainerByVMSlot(vmSlot, entry.getValue().getDlType()).setMoveOrder(entry.getValue().getMoveOrder());
                }
            }
        }
        for (Map.Entry<String, WorkMove> entry : moveData.getLoadWorkMoveMap().entrySet()) {
            for (VMSlot vmSlot : entry.getValue().getVmSlotSet()) {
                if (entry.getValue().getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    workingData.getVMContainerByVMSlot(vmSlot, entry.getValue().getDlType()).setMoveOrder(entry.getValue().getMoveOrder());
                }
            }
        }
    }

    public void generateCwpResult1(WorkingData workingData, SmartCwp3Results smartCwpResults) {
        Map<String, SmartReCwpModalInfo> smartReCwpModalInfoMap = new HashMap<>();
        for (String key : workingData.getWorkMoveMap().keySet()) {
            SmartReCwpModalInfo smartReCwpModalInfo = new SmartReCwpModalInfo();
            smartReCwpModalInfo.setModalName(key);
            smartReCwpModalInfo.setBerthId(workingData.getVmSchedule().getBerthId());
            smartReCwpModalInfoMap.put(key, smartReCwpModalInfo);
        }
        for (Map.Entry<String, List<WorkMove>> entry : workingData.getWorkMoveMap().entrySet()) {
            List<SmartReCwpWorkOrderInfo> smartReCwpWorkOrderInfoList = new ArrayList<>();
            for (WorkMove workMove : entry.getValue()) {
                if (workMove.getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    Set<VMContainer> vmContainerSet = new HashSet<>();
                    for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                        VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                        vmContainer.setMoveOrder(workMove.getMoveOrder());
                        vmContainer.setCntWorkTime(workMove.getWorkTime());
                        vmContainerSet.add(vmContainer);
                    }
                    for (VMContainer vmContainer : vmContainerSet) {
                        SmartReCwpWorkOrderInfo smartReCwpWorkOrderInfo = new SmartReCwpWorkOrderInfo();
                        smartReCwpWorkOrderInfo.setBerthId(vmContainer.getBerthId());
                        smartReCwpWorkOrderInfo.setCraneNo(workMove.getCraneNo());
                        smartReCwpWorkOrderInfo.setCranePosition(workMove.getWorkPosition());
                        smartReCwpWorkOrderInfo.setBayNo(String.format("%02d", workMove.getBayNo()));
                        smartReCwpWorkOrderInfo.setHatchId(vmContainer.getHatchId());
                        smartReCwpWorkOrderInfo.setVesselLocation(vmContainer.getvLocation());
                        smartReCwpWorkOrderInfo.setCszcsizecd(vmContainer.getSize());
                        smartReCwpWorkOrderInfo.setWorkflow(PublicMethod.getWorkFlowStr(workMove.getWorkFlow()));
                        smartReCwpWorkOrderInfo.setCwpwkmovenum(workMove.getMoveOrder());
                        smartReCwpWorkOrderInfo.setLduldfg(vmContainer.getDlType());
                        smartReCwpWorkOrderInfo.setWorkingStartTime(workMove.getPlanStartTime());
                        smartReCwpWorkOrderInfo.setWorkingEndTime(workMove.getPlanEndTime());
                        smartReCwpWorkOrderInfo.setMoveWorkTime(workMove.getWorkTime().intValue());
                        smartReCwpWorkOrderInfo.setQdc(vmContainer.getQdc());
                        smartReCwpWorkOrderInfo.setPlanAmount(1L);
                        smartReCwpWorkOrderInfo.setVpcCntrId(vmContainer.getVpcCntId());
                        smartReCwpWorkOrderInfoList.add(smartReCwpWorkOrderInfo);
                    }
                }
            }
            smartReCwpModalInfoMap.get(entry.getKey()).getSmartReCwpWorkOrderInfoList().addAll(smartReCwpWorkOrderInfoList);
        }
        for (Map.Entry<String, List<WorkBlock>> entry : workingData.getWorkBlockMap().entrySet()) {
            List<SmartReCwpBlockInfo> smartReCwpBlockInfoList = new ArrayList<>();
            for (WorkBlock workBlock : entry.getValue()) {
                SmartReCwpBlockInfo smartReCwpBlockInfo = new SmartReCwpBlockInfo();
                smartReCwpBlockInfo.setBerthId(workingData.getVmSchedule().getBerthId());
                smartReCwpBlockInfo.setCraneNo(workBlock.getCraneNo());
                smartReCwpBlockInfo.setBayNo(workBlock.getBayNo());
                smartReCwpBlockInfo.setHatchId(workBlock.getHatchId());
                smartReCwpBlockInfo.setPlanAmount(workBlock.getPlanAmount());
                smartReCwpBlockInfo.setLduldfg(workBlock.getLduldfg());
                smartReCwpBlockInfo.setWorkingStartTime(workBlock.getWorkingStartTime());
                smartReCwpBlockInfo.setWorkingEndTime(workBlock.getWorkingEndTime());
                smartReCwpBlockInfo.setSelectReason(workBlock.getSelectReason());
                smartReCwpBlockInfo.setBlockType(workBlock.getBlockType());
                smartReCwpBlockInfoList.add(smartReCwpBlockInfo);
            }
            CraneOrderMethod craneOrderMethod = new CraneOrderMethod();
            smartReCwpBlockInfoList = craneOrderMethod.getHatchSeq(smartReCwpBlockInfoList);
            smartReCwpBlockInfoList = craneOrderMethod.getCraneSeq(smartReCwpBlockInfoList);
            smartReCwpModalInfoMap.get(entry.getKey()).getSmartReCwpBlockInfoList().addAll(smartReCwpBlockInfoList);
        }
        for (Map.Entry<String, List<CraneEfficiency>> entry : workingData.getCraneEfficiencyMap().entrySet()) {
            List<SmartReCwpCraneEfficiencyInfo> smartReCwpCraneEfficiencyInfoList = new ArrayList<>();
            for (CraneEfficiency craneEfficiency : entry.getValue()) {
                SmartReCwpCraneEfficiencyInfo smartReCwpCraneEfficiencyInfo = new SmartReCwpCraneEfficiencyInfo();
                smartReCwpCraneEfficiencyInfo = (SmartReCwpCraneEfficiencyInfo) BeanCopyUtil.copyBean(craneEfficiency, smartReCwpCraneEfficiencyInfo);
                smartReCwpCraneEfficiencyInfoList.add(smartReCwpCraneEfficiencyInfo);
            }
            smartReCwpModalInfoMap.get(entry.getKey()).getSmartReCwpCraneEfficiencyInfoList().addAll(smartReCwpCraneEfficiencyInfoList);
        }
        smartCwpResults.getSmartReCwpModalInfoList().addAll(smartReCwpModalInfoMap.values());
    }
}
