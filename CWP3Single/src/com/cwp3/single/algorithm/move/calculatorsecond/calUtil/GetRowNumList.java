package com.cwp3.single.algorithm.move.calculatorsecond.calUtil;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;

import java.util.List;

/**
 * @author dongyuhang
 * @date 2018/7/24 10:52
 * @Description:
 * 根据装卸情况判断排号顺序
 */
public class GetRowNumList {
    private String dlType;
    private WorkingData workingData;
    private Long hatchId;
    private StructureData structureData;

    public GetRowNumList(String dlType, WorkingData workingData, Long hatchId, StructureData structureData) {
        this.dlType = dlType;
        this.workingData = workingData;
        this.hatchId = hatchId;
        this.structureData = structureData;
    }

    public List<Integer> getRowNumList(){
        //根据装卸情况得到该舱的排号顺序list
        String oddOrEven = null;
        if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_LAND_SEA);
        } else {
            oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_SEA_LAND);
        }
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, oddOrEven);
        return rowNoSeqList;
    }
}
