package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class M40Dual extends AbstractMaker {

    @Override
    public boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData) {
        boolean canDo = false;
        String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
        VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
        if (vmContainer != null && vmContainer.getSize().startsWith(getSize())) {
            VMSlot vmSlotNext = structureData.getSideVMSlot(vmSlot, oddOrEven); //todo:如何按分档进行编作业工艺
            if (vmSlotNext != null) {
                VMContainer vmContainerNext = workingData.getVMContainerByVMSlot(vmSlotNext, dlType);
                if (vmContainerNext != null && PublicMethod.hasNoneWorkFlow(vmContainerNext.getWorkFlow())) {
                    if (compareTwoContainer(vmContainer, vmContainerNext, workingData, structureData)) {
                        vmContainerNext.setWorkFlow(getWorkFlow());
                        canDo = true;
//                        long cntWorkTime = vmContainer.getCntWorkTime() > vmContainerNext.getCntWorkTime() ? vmContainer.getCntWorkTime() : vmContainerNext.getCntWorkTime();
                        long cntWorkTime = PublicMethod.getCntWorkTime(vmContainerNext, workingData.getCwpConfig());
                        WorkMove workMove = new WorkMove(dlType, getWorkFlow(), cntWorkTime, CWPDomain.MOVE_TYPE_CNT);
                        workMove.addVmSlot(vmSlot);
                        workMove.addVmSlot(vmSlotNext);
                        VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                        VMSlot vmSlotPairNext = structureData.getSideVMSlot(vmSlotPair, oddOrEven);
                        workMove.addVmSlot(vmSlotPair);
                        workMove.addVmSlot(vmSlotPairNext);
                        workMove.setHatchId(vmContainer.getHatchId());
                        workMove.setBayNo(vmSlot.getVmPosition().getBayNo());
                        workMove.setRowNo(vmSlot.getVmPosition().getRowNo());
                        workMove.setTierNo(vmSlot.getVmPosition().getTierNo());
                        //move属于哪个档
                        workMove.setHcSeq(workingData.getHcSeqByWorkMove(vmContainer.getHatchId(), workMove));
                        workingData.addWorkMove(workMove);
                    }
                }
            }
        }
        return canDo;
    }

    private boolean compareTwoContainer(VMContainer vmContainer, VMContainer vmContainerNext, WorkingData workingData, StructureData structureData) {
        boolean base = super.compareTwoContainer(vmContainer, vmContainerNext);
        boolean height = vmContainer.getIsHeight().equals(vmContainerNext.getIsHeight());
        return base && height;
    }

    @Override
    public String getWorkFlow() {
        return CWPCraneDomain.CT_DUAL40;
    }

    @Override
    public String getSize() {
        return "4";
    }
}
