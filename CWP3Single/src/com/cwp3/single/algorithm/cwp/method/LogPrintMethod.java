package com.cwp3.single.algorithm.cwp.method;

import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.model.log.Logger;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;

import java.util.List;

/**
 * Created by csw on 2017/11/14.
 * Description:
 */
public class LogPrintMethod {

    public static void printCraneDividedInfo(List<CWPCrane> cwpCranes, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            for (CWPCrane cwpCrane : cwpCranes) {
//                logger.logDebug("初始分块桥机(号,工艺,作业倍,左右范围hatchSeq,左右范围量): " + cwpCrane.getCraneNo() + ", " + cwpCrane.getDpCurrentWorkFlow() + ", " + cwpCrane.getDpCurrentWorkBayNo() + ", " + cwpCrane.getDpWorkHatchSeqFrom() + "—" + cwpCrane.getDpWorkHatchSeqTo() + ", " + cwpCrane.getDpWorkTimeFrom() + "—" + cwpCrane.getDpWorkTimeTo());
            }
        }
    }

    public static void printBayWorkTime(List<CWPBay> cwpBays, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            StringBuilder bayWorkTimeStr = new StringBuilder();
            for (CWPBay cwpBay : cwpBays) {
                bayWorkTimeStr.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpTotalWorkTime()).append(" ");
            }
            logger.logDebug("初始化每一个倍总量:" + bayWorkTimeStr);
        }
    }

    public static void printCurBayWorkTime(List<CWPBay> cwpBays, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            StringBuilder bayWorkTimeStr = new StringBuilder();
            StringBuilder bayStr = new StringBuilder();
            for (CWPBay cwpBay : cwpBays) {
                bayStr.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpAvailableWorkTime()).append(" ");
                bayWorkTimeStr.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpCurrentTotalWorkTime()).append(" ");
            }
            logger.logDebug("当前每个倍可作业量:" + bayStr);
            logger.logDebug("当前每个倍作业总量:" + bayWorkTimeStr);
        }
    }

    public static void printKeyAndDividedBay(List<CWPBay> cwpBays, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            StringBuilder keyBayNoStr = new StringBuilder();
            StringBuilder dividedBayStr = new StringBuilder();
            for (CWPBay cwpBay : cwpBays) {
//                if (cwpBay.getKeyBay()) {
//                    keyBayNoStr.append(cwpBay.getBayNo()).append(" ");
//                }
//                if (cwpBay.getDividedBay()) {
//                    dividedBayStr.append(cwpBay.getBayNo()).append(" ");
//                }
            }
            logger.logDebug("重点倍: " + keyBayNoStr);
            logger.logDebug("分割倍: " + dividedBayStr);
        }
    }

    public static void printSelectedCrane(List<CWPCrane> cwpCraneList, Logger logger) {
        StringBuilder selectedCraneNoStr = new StringBuilder("CWP algorithm selects cranes(No): ");
        for (CWPCrane craneNo : cwpCraneList) {
            selectedCraneNoStr.append(craneNo.getCraneNo()).append(" ");
        }
        logger.logInfo(selectedCraneNoStr.toString());
        if (CWPDefaultValue.outputLogToConsole) {
            StringBuilder bayNos = new StringBuilder();
            for (CWPCrane cwpCrane : cwpCraneList) {
                selectedCraneNoStr.setLength(0);
                bayNos.setLength(0);
                for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
                    bayNos.append(bayNo).append("、");
                }
                selectedCraneNoStr.append(cwpCrane.getCraneNo()).append(", ").append(cwpCrane.getDpCurrentWorkBayNo()).append(", ").
                        append(cwpCrane.getDpWorkBayNoFrom()).append("~").append(cwpCrane.getDpWorkBayNoTo()).append(", ").
                        append(cwpCrane.getDpWorkTimeFrom()).append("~").append(cwpCrane.getDpWorkTimeTo()).append(", ").
                        append(cwpCrane.getDpCurMeanWorkTime()).append(", ").
                        append(bayNos);
                logger.logDebug("作业范围(桥机号,作业倍,左右范围倍号,左右范围量,meanWt,bayNos): " + selectedCraneNoStr);
            }
        }
    }

    public static void printDpInfo1(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, DPResult[][] dp, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            int craneNum = cwpCranes.size();
            int bayNum = cwpBays.size();
            StringBuilder str = new StringBuilder("dp:");
            for (int t = 0; t < dp[craneNum - 1][bayNum - 1].getDpTraceBack().size(); t++) {
                str.append("(").append(dp[craneNum - 1][bayNum - 1].getDpTraceBack().get(t).getFirst()).append(",").append(dp[craneNum - 1][bayNum - 1].getDpTraceBack().get(t).getSecond()).append(")");
            }
            logger.logDebug(str.toString());
        }
    }

    public static void printCraneSelectBayInfo(String strInfo, CwpData cwpData, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            for (CWPCrane cwpCrane : cwpData.getDpCwpCraneList()) {
                StringBuilder str = new StringBuilder(strInfo + "(" + cwpCrane.getCraneNo() + "):");
                for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
                    DPPair dpPair = new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo());
                    DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(cwpData.getDpCraneSelectBays(), dpPair);
                    str.append(cwpBay.getBayNo()).append(":").append(dpCraneSelectBay.getDpFeature().getCode()).append(" ");
                }
                logger.logDebug(str.toString());
            }
        }
    }
}
