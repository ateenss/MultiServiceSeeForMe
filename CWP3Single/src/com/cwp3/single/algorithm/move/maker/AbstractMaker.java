package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;

/**
 * Created by csw on 2017/9/21.
 * Description:
 */
public abstract class AbstractMaker {

    public abstract boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData);

    public abstract String getWorkFlow();

    public abstract String getSize();

    boolean compareTwoContainer(VMContainer vmContainer, VMContainer vmContainerPair) {
        boolean size = vmContainer.getSize().equals(vmContainerPair.getSize()); //尺寸
        boolean type = vmContainer.getType().equals(vmContainerPair.getType()); //箱型
        return size && type;
    }
    
}
