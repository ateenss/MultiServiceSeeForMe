package com.cwp3.ioservice;

import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

/**
 * Created by csw on 2018/7/16.
 * Description:
 */
public interface CwpGeneratorService {

    void doPlanCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwpResults);
}
