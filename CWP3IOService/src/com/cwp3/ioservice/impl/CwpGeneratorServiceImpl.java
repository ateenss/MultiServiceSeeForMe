package com.cwp3.ioservice.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.ioservice.CwpGeneratorService;
import com.cwp3.ioservice.ParseDataService;
import com.cwp3.single.service.CwpService;
import com.cwp3.single.service.HatchBlockService;
import com.cwp3.single.service.MoveService;
import com.cwp3.single.service.impl.CwpServiceImpl;
import com.cwp3.single.service.impl.HatchBlockServiceImpl;
import com.cwp3.single.service.impl.MoveServiceImpl;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

/**
 * Created by csw on 2018/7/16.
 * Description:
 */
public class CwpGeneratorServiceImpl implements CwpGeneratorService {

    @Override
    public void doPlanCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwpResults) {
        ParseDataService parseDataService = new ParseDataServiceImpl();
        AllRuntimeData allRuntimeData = parseDataService.parseAllRuntimeData(smartCwpImportData);
        HatchBlockService hatchBlockService = new HatchBlockServiceImpl();
        MoveService moveService = new MoveServiceImpl();
        CwpService cwpService = new CwpServiceImpl();
        ResultGeneratorServiceImpl resultGeneratorService = new ResultGeneratorServiceImpl();
        for (Long berthId : allRuntimeData.getAllBerthId()) {
            hatchBlockService.makeHatchBlock(allRuntimeData, berthId);
            moveService.makeWorkFlow(allRuntimeData, berthId, null);
            cwpService.doPlanCwp(allRuntimeData, berthId);
            resultGeneratorService.generateCwpResult1(allRuntimeData.getWorkingDataByBerthId(berthId), smartCwpResults);
            smartCwpResults.getSmartReMessageInfo().setCwpVersion(CWPDefaultValue.VERSION);
            smartCwpResults.getSmartReMessageInfo().putErrorLog(berthId, allRuntimeData.getLogger().getError());
            smartCwpResults.getSmartReMessageInfo().putExecuteLog(berthId, allRuntimeData.getLogger().getInfo());
            smartCwpResults.getSmartReMessageInfo().putErrorLog(berthId, allRuntimeData.getWorkingDataByBerthId(berthId).getLogger().getError());
            smartCwpResults.getSmartReMessageInfo().putExecuteLog(berthId, allRuntimeData.getWorkingDataByBerthId(berthId).getLogger().getInfo());
        }

    }
}
