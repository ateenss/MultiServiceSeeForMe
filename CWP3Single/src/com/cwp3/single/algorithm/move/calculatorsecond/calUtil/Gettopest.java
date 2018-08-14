package com.cwp3.single.algorithm.move.calculatorsecond.calUtil;

import com.cwp3.data.single.StructureData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMBay;
import com.cwp3.model.vessel.VMHatch;
import com.cwp3.model.vessel.VMRow;

/**
 * @author dongyuhang
 * @date 2018/7/1910:09
 * @Description:
 * 拿到最高层的工具类
 */

public class Gettopest {
    private Long hatchId;
    private StructureData structureData;
    private VMHatch hatch;

    public Gettopest(Long hatchId,StructureData structureData){
        this.hatch = structureData.getVMHatchByHatchId(hatchId);
        this.structureData=structureData;
        this.hatchId=hatchId;
    }
    //    description:    第一次计算前返回甲板内最高层工具类
    public Integer getTopTierInHatch(){

        Integer topTier1=getVMBay_below(hatch.getBayNo1()+"@"+CWPDomain.BOARD_BELOW);
        Integer topTier2=getVMBay_below(hatch.getBayNo2()+"@"+CWPDomain.BOARD_BELOW);
        return topTier1<topTier2?topTier2:topTier1;
    }

    public Integer getVMBay_below(String BayKey){
        Integer topTier=0;
        VMBay bay_below = structureData.getVMBayByBayKey(BayKey);
        if(bay_below != null){
            for(Integer r:bay_below.getRowNoList()){
                VMRow row = bay_below.getVMRowByRowNo(r);
                if(row != null){
                    if(topTier.equals(0)){
                        topTier = row.getTopTierNo();
                    }else if(row.getTopTierNo()>topTier){
                        topTier = row.getTopTierNo();
                    }
                }
            }
        }

        return topTier;
    }
}
