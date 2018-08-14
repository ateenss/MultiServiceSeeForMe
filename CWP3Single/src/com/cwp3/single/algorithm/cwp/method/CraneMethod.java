package com.cwp3.single.algorithm.cwp.method;

import com.cwp3.model.crane.CMCraneAddOrDelete;
import com.cwp3.single.algorithm.cwp.modal.CWPBay;
import com.cwp3.single.algorithm.cwp.modal.CWPCrane;
import com.cwp3.single.algorithm.cwp.modal.DPPair;
import com.cwp3.single.algorithm.cwp.modal.DPResult;
import com.cwp3.single.data.CwpData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class CraneMethod {

    public static List<CWPCrane> getAvailableCranes(CwpData cwpData) {
        List<CWPCrane> cwpCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) {
            List<CMCraneAddOrDelete> cmCraneAddOrDeleteList = cwpData.getWorkingData().getCMCraneAddOrDeleteListByCraneNo(cwpCrane.getCraneNo());
            if (cmCraneAddOrDeleteList.size() > 0) {

            } else {
                cwpCraneList.add(cwpCrane);
            }
        }
        return cwpCraneList;
    }

    public static long obtainMinWorkTime(DPResult dpResult, CwpData cwpData) {
        if (dpResult.getDpTraceBack().isEmpty()) {
            return 0;
        }
        long minWorkTime = Long.MAX_VALUE;
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            CWPCrane cwpCrane = PublicMethod.getCwpCraneByCraneNo((String) dpPair.getFirst(), cwpData.getDpCwpCraneList());
            CWPBay cwpBay = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            long craneMinWorkTime = cwpBay.getDpAvailableWorkTime();
            if (cwpBay.getDpAvailableDiscWt() > 0 && cwpBay.getDpAvailableLoadWt() > 0) { //卸和装分开
                craneMinWorkTime = cwpBay.getDpAvailableDiscWt();
            }
            if (cwpCrane != null) {
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoFrom()) ) {
                    if (craneMinWorkTime > cwpCrane.getDpWorkTimeFrom() && cwpCrane.getDpWorkTimeFrom() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        craneMinWorkTime = cwpCrane.getDpWorkTimeFrom();
                    }
                }
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoTo())) {
                    if (craneMinWorkTime > cwpCrane.getDpWorkTimeTo() && cwpCrane.getDpWorkTimeTo() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        craneMinWorkTime = cwpCrane.getDpWorkTimeTo();
                    }
                }
            }
            minWorkTime = Math.min(minWorkTime, craneMinWorkTime);
        }
        return minWorkTime;
    }
}
