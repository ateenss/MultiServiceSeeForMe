package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.single.algorithm.cwp.CwpProcess;
import com.cwp3.single.service.CwpService;

/**
 * Created by csw on 2018/6/7.
 * Description:
 */
public class CwpServiceImpl implements CwpService {

    @Override
    public void doPlanCwp(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        workingData.setCwpType(CWPDomain.CWP_TYPE_PLAN);
        workingData.getLogger().logInfo("调用单船CWP算法，对船舶(berthId:" + berthId + ")进行CWP计划安排(" + workingData.getCwpType() + ")");
        try {
            CwpProcess cwpProcess = new CwpProcess(allRuntimeData);
            cwpProcess.processCwp(allRuntimeData, berthId);
        } catch (Exception e) {
            workingData.getLogger().logError("对船舶(berthId:" + berthId + ")进行CWP计划安排时发生异常");
            e.printStackTrace();
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")CWP计划安排结束");

    }

    @Override
    public void doWorkCwp(AllRuntimeData allRuntimeData, Long berthId) {

    }

    @Override
    public void doMultipleCwp(AllRuntimeData allRuntimeData, Long berthId) {

    }
}
