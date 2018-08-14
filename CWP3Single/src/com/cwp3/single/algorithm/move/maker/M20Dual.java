package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMContainerSlot;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class M20Dual extends AbstractMaker {

    @Override
    public boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData) {
        boolean canDo = false;
        VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
        if (!CWPDomain.SEPARATED_SLOT.equals(vmContainerSlot.getSize())) { //不是全隔槽的slot
            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
            if (vmContainer != null && vmContainer.getSize().startsWith(getSize())) {
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot); //对面slot
                if (vmSlotPair != null) {
                    VMContainer vmContainerPair = workingData.getVMContainerByVMSlot(vmSlotPair, dlType);
                    if (vmContainerPair != null && PublicMethod.hasNoneWorkFlow(vmContainerPair.getWorkFlow())) { //对面集装箱没有编写作业工艺
                        if (compareTwoContainer(vmContainer, vmContainerPair, workingData)) {
                            vmContainerPair.setWorkFlow(getWorkFlow());
                            canDo = true;
//                            long cntWorkTime = vmContainer.getCntWorkTime() > vmContainerPair.getCntWorkTime() ? vmContainer.getCntWorkTime() : vmContainerPair.getCntWorkTime();
                            long cntWorkTime = PublicMethod.getCntWorkTime(vmContainerPair, workingData.getCwpConfig());
                            WorkMove workMove = new WorkMove(dlType, getWorkFlow(), cntWorkTime, CWPDomain.MOVE_TYPE_CNT);
                            workMove.setHatchId(vmContainer.getHatchId());
                            workMove.addVmSlot(vmSlot);
                            workMove.addVmSlot(vmSlotPair);
                            workMove.setBayNo((vmSlot.getVmPosition().getBayNo() + vmSlotPair.getVmPosition().getBayNo()) / 2);
                            workMove.setRowNo(vmSlot.getVmPosition().getRowNo());
                            workMove.setTierNo(vmSlot.getVmPosition().getTierNo());
                            //move属于哪个档
                            workMove.setHcSeq(workingData.getHcSeqByWorkMove(vmContainer.getHatchId(), workMove));
                            workingData.addWorkMove(workMove);
                        }
                    }
                }
            }
        }
        return canDo;
    }

    @Override
    public String getWorkFlow() {
        return CWPCraneDomain.CT_DUAL20;
    }

    @Override
    public String getSize() {
        return "2";
    }

    private boolean compareTwoContainer(VMContainer vmContainer, VMContainer vmContainerPair, WorkingData workingData) {
        boolean base = super.compareTwoContainer(vmContainer, vmContainerPair);
        //重量差
        boolean weight = true;
        if (vmContainer.getWeightKg() != null && vmContainerPair.getWeightKg() != null) {
            weight = Math.abs(vmContainer.getWeightKg() - vmContainerPair.getWeightKg()) < workingData.getCwpConfig().getTwinWeightDiff();
        }
        //特殊箱型
        boolean type = !CWPDomain.CNT_TYPE_TK.equals(vmContainer.getType()) && !CWPDomain.CNT_TYPE_FR.equals(vmContainer.getType());
        return base && weight && type;
    }

}
