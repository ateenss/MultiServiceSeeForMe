package com.cwp3.single.algorithm.cwp;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.log.Logger;
import com.cwp3.single.algorithm.cwp.decision.Analyzer;
import com.cwp3.single.algorithm.cwp.decision.Dynamic;
import com.cwp3.single.algorithm.cwp.method.CraneMethod;
import com.cwp3.single.algorithm.cwp.decision.Evaluator;
import com.cwp3.single.algorithm.cwp.method.LogPrintMethod;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;
import com.cwp3.single.method.CwpDataMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2018/5/29.
 * Description:
 */
public class CwpProcess {

    private CwpDataMethod cwpDataMethod;
    private Analyzer analyzer;
    private Evaluator evaluator;
    private Dynamic dynamic;

    public CwpProcess(AllRuntimeData allRuntimeData) {
        cwpDataMethod = new CwpDataMethod(allRuntimeData);
        analyzer = new Analyzer();
        evaluator = new Evaluator();
        dynamic = new Dynamic();
    }

    public void processCwp(AllRuntimeData allRuntimeData, Long berthId) {
        Logger logger = allRuntimeData.getWorkingDataByBerthId(berthId).getLogger();
        logger.logInfo("CWP algorithm is starting...");
        long st = System.currentTimeMillis();

        CwpData cwpData = cwpDataMethod.initCwpData(berthId);

        List<CwpData> cwpDataList = new ArrayList<>();
        firstSearch(cwpData, cwpDataList);

        if (cwpDataList.size() == 0) {
            logger.logError("无法得到满足船期条件的最优结果！");
            return;
        }

        List<CwpData> cwpDataResultList = evaluator.getBestResult(cwpDataList);

        cwpDataMethod.generateResult(cwpDataResultList);

        long et = System.currentTimeMillis();
        logger.logInfo("CWP algorithm finished. The running time of algorithm is " + (et - st) / 1000 + "s");
    }

    private void firstSearch(CwpData cwpData, List<CwpData> cwpDataList) {
        Logger logger = cwpData.getWorkingData().getLogger();

        cwpDataMethod.computeCurrentWorkTime(cwpData);

        analyzer.firstAnalyzeCwpBay(cwpData);
        LogPrintMethod.printCurBayWorkTime(cwpData.getAllCWPBays(), logger);

        boolean finish = true;
        if (finish(1, cwpData)) {
            finish = false;
        }
        if (finish) {
            cwpDataList.add(cwpData);
            return;
        }

        analyzer.firstAnalyzeCwpCrane(cwpData);

        List<DPBranch> dpBranchList = evaluator.getFirstDpBranchList1(cwpData);

        for (int i = 0; i < dpBranchList.size(); i++) {
            logger.logInfo("The first search，branch(" + i + "):======================================================");
            CwpData cwpDataCopy = cwpDataMethod.copyCwpData(cwpData);
            cwpDataCopy.setDpCwpCraneList(dpBranchList.get(i).getDpCwpCraneList());
            LogPrintMethod.printSelectedCrane(cwpDataCopy.getDpCwpCraneList(), logger);
            cwpDataCopy.setDpCraneSelectBays(dpBranchList.get(i).getDpCraneSelectBays());

            LogPrintMethod.printCraneSelectBayInfo("桥机倍位特征值", cwpDataCopy, logger);
            DPResult dpResult = dynamic.cwpKernel(cwpDataCopy.getDpCwpCraneList(), cwpDataCopy.getAllCWPBays(), cwpDataCopy);

            long minWorkTime = CraneMethod.obtainMinWorkTime(dpResult, cwpDataCopy);

            realWork(dpResult, minWorkTime, cwpDataCopy);

            cwpDataCopy.setDpResult(dpResult);
            search(cwpDataCopy, cwpDataList, 2);
        }
    }

    private void search(CwpData cwpData, List<CwpData> cwpDataList, int depth) {
        Logger logger = cwpData.getWorkingData().getLogger();
        logger.logDebug("第" + depth + "次search:------------------------------------");

        cwpDataMethod.computeCurrentWorkTime(cwpData);

        analyzer.analyzeCwpBay(cwpData);
        LogPrintMethod.printCurBayWorkTime(cwpData.getAllCWPBays(), logger);

        boolean finish = true;
        if (finish(depth, cwpData)) {
            finish = false;
        }
        if (finish) {
            cwpDataList.add(cwpData);
            return;
        }

        analyzer.analyzeCwpCrane(cwpData);
        LogPrintMethod.printSelectedCrane(cwpData.getDpCwpCraneList(), logger);

        if (evaluator.invalidBranch(cwpData)) {
            return;
        }

        DPBranch dpBranch = evaluator.getCurDpBranch(cwpData);
        cwpData.setDpCraneSelectBays(dpBranch.getDpCraneSelectBays());

        LogPrintMethod.printCraneSelectBayInfo("桥机倍位特征值", cwpData, logger);
        DPResult dpResult = dynamic.cwpKernel(cwpData.getDpCwpCraneList(), cwpData.getAllCWPBays(), cwpData);

        long minWorkTime = CraneMethod.obtainMinWorkTime(dpResult, cwpData);

        long realWorkTime = realWork(dpResult, minWorkTime, cwpData);
        if (!cwpData.getFirstDoCwp() && realWorkTime == 0) {
            return;
        }

        cwpData.setDpResult(dpResult);
        search(cwpData, cwpDataList, depth + 1);
    }

    private boolean finish(int depth, CwpData cwpData) {
        boolean isFinish = true;
        StringBuilder strBuilder = new StringBuilder("bayNo: ");
        for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                isFinish = false;
                strBuilder.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpCurrentTotalWorkTime()).append("-").append(cwpBay.getDpAvailableWorkTime()).append(" ");
            }
        }
        int d = 80;
        isFinish = depth > d || isFinish;
        if (isFinish) {
            if (depth > d) {
                cwpData.getWorkingData().getLogger().logError("CWP算法没有排完所有箱子的计划，请检查倍位(" + strBuilder.toString() + ")！");
                return false;
            }
        }
        return !isFinish;
    }

    private long realWork(DPResult dpResult, long minWorkTime, CwpData cwpData) {
        long maxRealWorkTime = 0;
        CwpConfig cwpConfig = cwpData.getWorkingData().getCwpConfig();
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            CWPCrane cwpCrane = PublicMethod.getCwpCraneByCraneNo((String) dpPair.getFirst(), cwpData.getDpCwpCraneList());
            CWPBay cwpBay = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(cwpData.getDpCraneSelectBays(), dpPair);
            if (cwpCrane != null && dpCraneSelectBay != null) { //can not be null
                cwpCrane.setDpCurrentWorkPosition(cwpBay.getWorkPosition());
                cwpCrane.setDpCurrentWorkBayNo(cwpBay.getBayNo());
                long moveTime = 0L;
                if (!cwpData.getFirstDoCwp()) { //不是第一次dp决策，要计算桥机移动时间
                    moveTime += (long) (dpCraneSelectBay.getDpDistance() / cwpConfig.getCraneMoveSpeed());
                    if (dpCraneSelectBay.getTroughMachine()) { //过驾驶台的移动时间
                        moveTime += cwpConfig.getCrossBarTime();
                    }
//                    if (cwpCrane.getDpNewAddFlag()) { //中途添加的桥机
//                        moveTime = 0;
//                    }
                    cwpCrane.addDpCurrentTime(moveTime);
                    Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
                    if (bayNoLast != null && !cwpBay.getBayNo().equals(bayNoLast)) { //扫舱时间
                        cwpCrane.addDpCurrentTime(cwpData.getWorkingData().getCwpConfig().getHatchScanTime());
                        cwpData.setDpMoveNumber(cwpData.getDpMoveNumber() + 1);
                    }
                } else {
                    cwpCrane.setDpCurrentTime(cwpData.getDpCurrentTime());
                }
                long realMinWorkTime;
                if (minWorkTime > moveTime) {
                    realMinWorkTime = minWorkTime - moveTime;
                } else {
                    if (!cwpData.getFirstDoCwp() && dpCraneSelectBay.getTroughMachine()) {//桥机置为正在移动状态
                        cwpData.getWorkingData().getLogger().logDebug("桥机(" + cwpCrane.getCraneNo() + ")正在过驾驶台！");
                    }
                    realMinWorkTime = 0L;
                }
                long realWorkTime = cwpDataMethod.doProcessOrder(cwpCrane, cwpBay, dpCraneSelectBay.getDpFeature().getDesc(), realMinWorkTime, cwpData);
                maxRealWorkTime = Math.max(maxRealWorkTime, realWorkTime);
                cwpCrane.addDpCurrentTime(realWorkTime);
                cwpCrane.setDpCurMeanWorkTime(cwpCrane.getDpCurMeanWorkTime() - realMinWorkTime);
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoFrom())) {
                    if (cwpCrane.getDpWorkTimeFrom() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        cwpCrane.setDpWorkTimeFrom(cwpCrane.getDpWorkTimeFrom() - realWorkTime);
                    }
                }
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoTo())) {
                    if (cwpCrane.getDpWorkTimeTo() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        cwpCrane.setDpWorkTimeTo(cwpCrane.getDpWorkTimeTo() - realWorkTime);
                    }
                }
            }
        }
        cwpData.getWorkingData().getLogger().logDebug("决策作业时间：" + maxRealWorkTime);
        boolean isFirstRealWork = !(maxRealWorkTime > 0) && cwpData.getFirstDoCwp();
        cwpData.setFirstDoCwp(isFirstRealWork);
        List<CWPCrane> cwpCranes = cwpData.getDpCwpCraneList();
        long maxCurrentTime = Long.MIN_VALUE;
        for (CWPCrane cwpCrane : cwpCranes) {
            maxCurrentTime = Math.max(maxCurrentTime, cwpCrane.getDpCurrentTime());
        }
        cwpData.setDpCurrentTime(maxCurrentTime);
        for (CWPCrane cwpCrane : cwpCranes) {
            if (PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), dpResult) == null) { //没有被选择作业的桥机当前时间需要增加
                cwpCrane.setDpCurrentTime(cwpData.getDpCurrentTime());
            }
        }
        return maxRealWorkTime;
    }
}
