package com.cwp3.single.algorithm.cwp.decision;

import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.method.CraneMethod;
import com.cwp3.single.algorithm.cwp.method.LogPrintMethod;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.CWPBay;
import com.cwp3.single.algorithm.cwp.modal.CWPCrane;
import com.cwp3.single.data.CwpData;
import com.cwp3.utils.CalculateUtil;
import com.cwp3.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/6/14.
 * Description:
 */
public class Analyzer {

    public void firstAnalyzeCwpBay(CwpData cwpData) {
        analyzeCwpBay(cwpData);
    }

    public void analyzeCwpBay(CwpData cwpData) {
        List<CWPBay> cwpBayList = cwpData.getAllCWPBays();
        for (CWPBay cwpBay : cwpBayList) {
            //总量分析
            long totalWt = 0;
            long steppingCntTotalWt = 0;
            Map<Integer, List<WorkMove>> tolWorkMoveMap = cwpData.getMoveResults().getTolWorkMoveMapByBayNo(cwpBay.getBayNo());
            for (List<WorkMove> workMoveList : tolWorkMoveMap.values()) {
                for (WorkMove workMove : workMoveList) {
                    totalWt += workMove.getWorkTime();
                    if (workMove.getVmSlotSet().size() == 1) { //判断move是垫脚
                        if (cwpData.getStructureData().isSteppingVMSlot(workMove.getOneVMSlot())) {
                            steppingCntTotalWt += workMove.getWorkTime();
                        }
                    }
                }
            }
            cwpBay.setDpCurrentTotalWorkTime(totalWt);
            if (cwpData.getFirstDoCwp()) {
                cwpBay.setDpTotalWorkTime(totalWt);
            }
            //可作业量分析
            long availableWt = 0;
            long steppingCntAvailableWt = 0;
            long availableDiscWt = 0, availableLoadWt = 0;
            Map<Integer, List<WorkMove>> availableWorkMoveMap = cwpData.getMoveResults().getAvailableWorkMoveMapByBayNo(cwpBay.getBayNo());
            for (List<WorkMove> workMoveList : availableWorkMoveMap.values()) { //分档进行可作业量分析
                for (WorkMove workMove : workMoveList) {
                    availableWt += workMove.getWorkTime();
                    if (workMove.getVmSlotSet().size() == 1 && cwpData.getStructureData().isSteppingVMSlot(workMove.getOneVMSlot())) { //判断move是垫脚
                        steppingCntAvailableWt += workMove.getWorkTime();
                    }
                    if (workMove.getBayNo() % 2 == 0) { //大倍位上的箱子
                        availableDiscWt += workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC) ? workMove.getWorkTime() : 0;
                        availableLoadWt += workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD) ? workMove.getWorkTime() : 0;
                    }
                }
            }
            //判断垫脚箱可以连续作业完，//判断这个舱内垫脚箱作业的最早时间和最晚时间
            cwpBay.setDpSteppingCntFlag(steppingCntAvailableWt > 0 && steppingCntTotalWt == steppingCntAvailableWt);
            //判断卸完大倍位上的箱子不能接着在大倍位上装，需要进行垫脚箱作业的判断
            cwpBay.setDpAvailableDiscWt(availableDiscWt);
            cwpBay.setDpAvailableLoadWt(availableLoadWt);
            cwpBay.setDpAvailableWorkTime(availableWt);
        }
    }

    public void firstAnalyzeCwpCrane(CwpData cwpData) {
        Long totalWorkTime = PublicMethod.getCurTotalWorkTime(cwpData.getAllCWPBays());
        Long vesselTime = DateUtil.getSecondTime(cwpData.getWorkingData().getVmSchedule().getPlanEndWorkTime()) - cwpData.getDpCurrentTime();
        int minCraneNum = 0;
        if (vesselTime > 0) {
            minCraneNum = (int) Math.ceil(totalWorkTime.doubleValue() / (vesselTime.doubleValue()));
        } else { //当前时间超过船期时间
            cwpData.getWorkingData().getLogger().logInfo("当前时间超过船期结束时间");
        }
        int maxCraneNum = PublicMethod.getMaxCraneNum(cwpData.getAllCWPBays(), cwpData);
        cwpData.getWorkingData().getLogger().logInfo("Minimum number of crane is: " + minCraneNum + ", maximum number of crane is: " + maxCraneNum);
        int craneNum = minCraneNum > maxCraneNum ? maxCraneNum : minCraneNum;
        List<CWPCrane> availableCraneList = CraneMethod.getAvailableCranes(cwpData); //todo:在该时刻可以作业的桥机
        craneNum = availableCraneList.size() > maxCraneNum ? maxCraneNum : availableCraneList.size();
        List<CWPCrane> dpCraneList = new ArrayList<>();
        for (int i = 0; i < craneNum; i++) {
            dpCraneList.add(availableCraneList.get(i));
        }
        List<CWPCrane> defaultCwpCraneList = PublicMethod.copyCwpCraneList(dpCraneList);
        analyzeCraneMoveRange(defaultCwpCraneList, cwpData.getAllCWPBays(), cwpData);
        LogPrintMethod.printSelectedCrane(defaultCwpCraneList, cwpData.getWorkingData().getLogger());
        if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getCraneSameWorkTime())) {
            cwpData.getDpFirstCwpCraneList().add(defaultCwpCraneList);
        } else { //去掉多余的分割量
            removeRedundantDividedBay(defaultCwpCraneList, cwpData.getAllCWPBays(), cwpData);
            LogPrintMethod.printSelectedCrane(defaultCwpCraneList, cwpData.getWorkingData().getLogger());
        }
        List<CWPCrane> maxRoadCwpCraneList = PublicMethod.copyCwpCraneList(dpCraneList);
        boolean dividedWithMaxRoad = analyzeCraneMoveRangeWithMaxRoad(maxRoadCwpCraneList, cwpData.getAllCWPBays(), cwpData);
        if (dividedWithMaxRoad) {
            LogPrintMethod.printSelectedCrane(maxRoadCwpCraneList, cwpData.getWorkingData().getLogger());
            if (CWPDomain.NO.equals(cwpData.getWorkingData().getCwpConfig().getCraneSameWorkTime())) {
                removeRedundantDividedBay(maxRoadCwpCraneList, cwpData.getAllCWPBays(), cwpData);
                LogPrintMethod.printSelectedCrane(maxRoadCwpCraneList, cwpData.getWorkingData().getLogger());
            }
            if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getMainRoadOneCrane())) {
                cwpData.getDpFirstCwpCraneList().add(maxRoadCwpCraneList);
            }
        }
        //如果有桥机上/下路，应该在第一次划分范围时提前预留作业倍位

        if (cwpData.getDpFirstCwpCraneList().size() == 0) { //没有策略参数，设置默认的策略
            cwpData.getDpFirstCwpCraneList().add(defaultCwpCraneList);
            cwpData.getDpFirstCwpCraneList().add(maxRoadCwpCraneList);
        }

        //分析桥机第一次该选择哪些倍位
        for (List<CWPCrane> cwpCraneList : cwpData.getDpFirstCwpCraneList()) {
            for (CWPCrane cwpCrane : cwpCraneList) {
                cwpCrane.getDpCurCanSelectBays().addAll(cwpCrane.getDpFirstCanSelectBays());
            }
            for (CWPCrane cwpCrane : cwpCraneList) {
                List<Integer> bayNos = new ArrayList<>();
                for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
                    if (cwpData.getCWPBayByBayNo(bayNo).getDpAvailableWorkTime() > 0) {
                        if (craneSplitBay(cwpCrane, bayNo, cwpCraneList, cwpData)) {
                            bayNos.add(bayNo);
                        }
                    }
                }
                if (bayNos.size() > 2) {
                    if (!bayNos.get(0).equals(cwpCrane.getDpFirstCanSelectBays().getLast())) {
                        cwpCrane.getDpSelectBays().add(bayNos.get(0));
                    }
                    if (!bayNos.get(1).equals(cwpCrane.getDpFirstCanSelectBays().getLast())) {
                        cwpCrane.getDpSelectBays().add(bayNos.get(1));
                    }
                }
                //是否可以优先作业驾驶台一边的倍位

                //根据每个倍位的垫脚最早作业时间和最晚作业时间，通过一开始选择倍位进行合理避开垫脚作业冲突
                //装卸船平衡参数，按开路比例进行划分
            }
        }
    }

    private void analyzeCraneMoveRange(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        int maxCraneNum = PublicMethod.getMaxCraneNum(cwpBays, cwpData);
        if (maxCraneNum == cwpCranes.size()) {
            divideCraneMoveRangeWithMaxCraneNum(cwpCranes, cwpBays, cwpData);
        } else {
            divideCraneMoveRangeWithAverageWt(cwpCranes, cwpBays, cwpData);
        }
    }

    private void divideCraneMoveRangeWithMaxCraneNum(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        int craneNum = cwpCranes.size();
        int bayNum = cwpBays.size();
        long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
        if (craneNum <= 0 || bayNum <= 0 || allWorkTime == 0) {
            return;
        }
        cwpData.getWorkingData().getLogger().logInfo("Divide crane move range with maximum number of cranes");
        int c = 0;
        for (int j = 0; j < bayNum; ) {
            c = c == craneNum ? craneNum - 1 : c;
            CWPBay cwpBayJ = cwpBays.get(j);
            CWPCrane cwpCrane = cwpCranes.get(c);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                cwpCrane.setDpWorkBayNoFrom(cwpBayJ.getBayNo());
                int k = j;
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance >= 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance()) {
                        break;
                    } else {
                        cwpCrane.setDpWorkBayNoTo(cwpBayK.getBayNo());
                        cwpCrane.getDpFirstCanSelectBays().add(cwpBayK.getBayNo());
                        cwpCrane.addDpCurMeanWorkTime(cwpBayK.getDpCurrentTotalWorkTime());
                    }
                }
                j = k;
                c++;
            } else {
                j++;
            }
        }
    }

    private void divideCraneMoveRangeWithAverageWt(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        int craneNum = cwpCranes.size();
        int bayNum = cwpBays.size();
        long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
        if (craneNum <= 0 || bayNum <= 0 || allWorkTime == 0) {
            return;
        }
        int realBayNum = 0;
        for (CWPBay cwpBay : cwpBays) {
            realBayNum = cwpBay.getDpCurrentTotalWorkTime() > 0 ? realBayNum + 1 : realBayNum;
        }
        long mean = realBayNum < craneNum ? allWorkTime / realBayNum : allWorkTime / craneNum;
        long meanLittleOrMore = 0L;
        cwpData.getWorkingData().getLogger().logInfo("Divide crane move range with average wt, all workTime：" + allWorkTime + ", mean workTime：" + mean);
        int c = 0;
        int cSize = 0;
        long tmpWorkTime = 0L;
        long amount = 15;
        amount = amount * CWPDefaultValue.oneCntWorkTime;
        amount = mean <= amount ? 0 : amount;
        long meanL = mean - amount + 10;
        long meanR = mean + amount - 10;
        for (CWPCrane cwpCrane : cwpCranes) {
            cwpCrane.getDpFirstCanSelectBays().clear();
        }
        for (int j = 0; j < bayNum; j++) {
            CWPBay cwpBay = cwpBays.get(j);
            cSize += 1;
            tmpWorkTime += cwpBay.getDpCurrentTotalWorkTime();
            c = c == craneNum ? craneNum - 1 : c;
            CWPCrane cwpCrane = cwpCranes.get(c);
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                cwpCrane.setDpWorkBayNoTo(cwpBay.getBayNo());
                cwpCrane.getDpFirstCanSelectBays().add(cwpBay.getBayNo());
                cwpCrane.addDpCurMeanWorkTime(cwpBay.getDpCurrentTotalWorkTime());
            }
            if (tmpWorkTime >= meanL && tmpWorkTime <= meanR) {
                cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                if (cwpBay.getDpCurrentTotalWorkTime() == 0 && cSize > 0) {
                    cwpCrane.setDpWorkBayNoTo(cwpBay.getBayNo());
                }
                meanLittleOrMore = (tmpWorkTime - mean) / craneNum - c - 1;
                tmpWorkTime = 0L;
                cSize = 0;
                c++;
            } else if (tmpWorkTime > meanR) {
                cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                if (cwpBay.getDpCurrentTotalWorkTime() == 0 && cSize > 0) {
                    cwpCrane.setDpWorkBayNoTo(cwpBay.getBayNo());
                }

                mean += 0L - meanLittleOrMore;
                long wt = cwpBay.getDpCurrentTotalWorkTime() - tmpWorkTime + mean;
                Long dwt = getDividedWorkTime(wt, cwpBay, cwpData); //甲板作为分割量
                wt = dwt != null ? dwt : wt;
                if (c < craneNum - 1) {
                    cwpCrane.setDpWorkTimeTo(wt);
                }
                tmpWorkTime = cwpBay.getDpCurrentTotalWorkTime() - wt;
                c++;
                if (c < craneNum) {
                    CWPCrane cwpCraneNext = cwpCranes.get(c);
                    cwpCraneNext.setDpWorkTimeFrom(tmpWorkTime);
                    cwpCraneNext.getDpFirstCanSelectBays().add(cwpBay.getBayNo());
                    cwpCraneNext.addDpCurMeanWorkTime(cwpBay.getDpCurrentTotalWorkTime());
                }
                cSize = 1;
            } else {
                if (c == craneNum - 1 && j == bayNum - 1) { //???
                    if (cwpCrane.getDpWorkBayNoFrom() == null) {
                        cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                    }
                    if (cwpCrane.getDpWorkBayNoTo() == null) {
                        CWPBay nextBay = PublicMethod.getNextBay(cwpBays.get(j + 1 - cSize), cwpData);
                        cwpCrane.setDpWorkBayNoTo(nextBay.getBayNo());
                    }
                }
            }
        }
    }

    private boolean analyzeCraneMoveRangeWithMaxRoad(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        int craneNum = cwpCranes.size();
        int bayNum = cwpBays.size();
        long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
        if (craneNum <= 0 || bayNum <= 0 || allWorkTime == 0) {
            return false;
        }
        long mean = allWorkTime / craneNum;
        cwpData.getWorkingData().getLogger().logInfo("Divide crane move range with max road( or reduce unnecessary dividing)......");
        List<CWPBay> maxCwpBayList = PublicMethod.getMaxWorkTimeCWPBayList(cwpBays, cwpData);
        long maxWorkTime = PublicMethod.getCurTotalWorkTime(maxCwpBayList);
        boolean divideWithMaxRoad = false;
        //最大量比平均量大或者接近平均量
        if (maxWorkTime > mean || Math.abs(mean - maxWorkTime) < 3600) {
            List<CWPBay> leftCwpBayList = PublicMethod.getSideCwpBayList(CWPDomain.L, maxCwpBayList, cwpBays);
            List<CWPBay> rightCwpBayList = PublicMethod.getSideCwpBayList(CWPDomain.R, maxCwpBayList, cwpBays);
            Long leftWorkTime = PublicMethod.getCurTotalWorkTime(leftCwpBayList);
            Long rightWorkTime = PublicMethod.getCurTotalWorkTime(rightCwpBayList);
            int leftCraneNum = (int) Math.ceil(leftWorkTime.doubleValue() / cwpData.getVesselTime().doubleValue());
            int rightCraneNum = (int) Math.ceil(rightWorkTime.doubleValue() / cwpData.getVesselTime().doubleValue());
            Integer redundantNum = craneNum - leftCraneNum - rightCraneNum - 1;
            if (redundantNum >= 0) { //桥机数目有多余，说明两边的桥机够用
                int up = (int) Math.ceil(redundantNum.doubleValue() / 2.0);
                int down = redundantNum / 2;
                leftCraneNum = leftCraneNum > rightCraneNum ? leftCraneNum + up : leftCraneNum + down;
                rightCraneNum = rightCraneNum > leftCraneNum ? rightCraneNum + up : rightCraneNum + down;
                if (craneNum == leftCraneNum + rightCraneNum + 1) {
                    List<CWPCrane> leftCwpCraneList = new ArrayList<>();
                    List<CWPCrane> maxRoadCwpCraneList = new ArrayList<>();
                    List<CWPCrane> rightCwpCraneList = new ArrayList<>();
                    int i = 0;
                    for (; i < leftCraneNum; i++) {
                        leftCwpCraneList.add(cwpCranes.get(i));
                    }
                    maxRoadCwpCraneList.add(cwpCranes.get(i++));
                    for (; i < craneNum; i++) {
                        rightCwpCraneList.add(cwpCranes.get(i));
                    }
                    analyzeCraneMoveRange(leftCwpCraneList, leftCwpBayList, cwpData);
                    divideCraneMoveRangeWithMaxCraneNum(maxRoadCwpCraneList, maxCwpBayList, cwpData);
                    analyzeCraneMoveRange(rightCwpCraneList, rightCwpBayList, cwpData);
                    divideWithMaxRoad = true;
                } else {
                    cwpData.getWorkingData().getLogger().logInfo("Failed! (" + (craneNum - 1) + ":" + leftCraneNum + "+" + rightCraneNum + ").");
                }
            } else {
                cwpData.getWorkingData().getLogger().logInfo("Failed! the number of cranes is not enough");
            }
        } else {
            cwpData.getWorkingData().getLogger().logInfo("Failed! max workTime < mean workTime");
        }
        return divideWithMaxRoad;
    }

    private void removeRedundantDividedBay(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        //保证船期的前提下（桥机范围内作业量小于船期），去掉没必要的分割量
        for (CWPCrane cwpCrane : cwpCranes) {
            Integer bayNoFrom = null, bayNoTo = null;
            if (cwpCrane.getDpWorkTimeFrom() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                CWPCrane frontCrane = PublicMethod.getFrontCrane(cwpCrane, cwpCranes);
                if (frontCrane != null && cwpCrane.getDpWorkTimeFrom() > frontCrane.getDpWorkTimeTo()) {
                    if (frontCrane.getDpWorkTimeTo() < 3600) {
                        frontCrane.getDpFirstCanSelectBays().removeLast();
                        frontCrane.setDpWorkBayNoTo(frontCrane.getDpFirstCanSelectBays().getLast());
                        frontCrane.setDpWorkTimeTo(0L);
                        cwpCrane.setDpWorkTimeFrom(0L);
                    }
                }
            }
            if (cwpCrane.getDpWorkTimeTo() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                CWPCrane nextCrane = PublicMethod.getNextCrane(cwpCrane, cwpCranes);
                if (nextCrane != null && cwpCrane.getDpWorkTimeTo() > nextCrane.getDpWorkTimeFrom()) {
                    if (nextCrane.getDpWorkTimeFrom() < 3600) {
                        nextCrane.getDpFirstCanSelectBays().removeFirst();
                        nextCrane.setDpWorkBayNoFrom(nextCrane.getDpFirstCanSelectBays().getFirst());
                        nextCrane.setDpWorkTimeFrom(0L);
                        cwpCrane.setDpWorkTimeTo(0L);
                    }
                }
            }
        }
    }

    public void analyzeCwpCrane(CwpData cwpData) {
        Long totalWorkTime = PublicMethod.getCurTotalWorkTime(cwpData.getAllCWPBays());
        Long vesselTime = DateUtil.getSecondTime(cwpData.getWorkingData().getVmSchedule().getPlanEndWorkTime()) - cwpData.getDpCurrentTime();
        int minCraneNum = 0;
        if (vesselTime > 0) {
            minCraneNum = (int) Math.ceil(totalWorkTime.doubleValue() / (vesselTime.doubleValue()));
        } else { //当前时间超过船期时间
            cwpData.getWorkingData().getLogger().logInfo("当前时间超过船期结束时间");
        }
        int maxCraneNum = PublicMethod.getMaxCraneNum(cwpData.getAllCWPBays(), cwpData);
        cwpData.getWorkingData().getLogger().logDebug("Minimum number of crane is: " + minCraneNum + ", maximum number of crane is: " + maxCraneNum);
        int craneNum = minCraneNum > maxCraneNum ? maxCraneNum : minCraneNum;
//        List<CWPCrane> availableCraneList = cwpData.getDpCwpCraneList(); //在该时刻可以作业的桥机
        //两边桥机提前下路的判断
        List<CWPCrane> availableCraneList = analyzeAutoDelCrane(cwpData.getDpCwpCraneList(), cwpData);
        //针对分割倍位的量，改变桥机作业范围
        changeCraneMoveRangeByDividedBay(availableCraneList, cwpData);
        for (CWPCrane cwpCrane : availableCraneList) {
            cwpCrane.getDpCurCanSelectBays().clear();
            cwpCrane.getDpCurCanSelectBays().addAll(cwpCrane.getDpFirstCanSelectBays());
        }
        //分析桥机作业范围内，桥机选择倍位的合理性进行处理
        analyzeCraneCurCanSelectBay(availableCraneList, cwpData);
        cwpData.setDpCwpCraneList(availableCraneList);
    }

    private List<CWPCrane> analyzeAutoDelCrane(List<CWPCrane> cwpCraneList, CwpData cwpData) {
        List<CWPCrane> availableCraneList = new ArrayList<>(cwpCraneList);

        return availableCraneList;
    }

    private void changeCraneMoveRangeByDividedBay(List<CWPCrane> cwpCraneList, CwpData cwpData) {
        for (CWPCrane cwpCrane : cwpCraneList) {
            List<Integer> bayNoRemoves = new ArrayList<>();
            for (Integer bayNo : cwpCrane.getDpFirstCanSelectBays()) {
                CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                if (PublicMethod.isDividedBay(cwpCrane, cwpBay, cwpCraneList)) {
                    if (bayNo.equals(cwpCrane.getDpWorkBayNoFrom())) {
                        CWPBay cwpBaySide = removeDividedBay(cwpCrane, cwpData, CWPDomain.L);
                        if (cwpBaySide != null) {
                            bayNoRemoves.add(bayNo);
                            cwpCrane.setDpWorkBayNoFrom(cwpBaySide.getBayNo());
                        }
                    }
                    if (bayNo.equals(cwpCrane.getDpWorkBayNoTo())) {
                        CWPBay cwpBaySide = removeDividedBay(cwpCrane, cwpData, CWPDomain.R);
                        if (cwpBaySide != null) {
                            bayNoRemoves.add(bayNo);
                            cwpCrane.setDpWorkBayNoTo(cwpBaySide.getBayNo());
                        }
                    }
                }
            }
            cwpCrane.getDpFirstCanSelectBays().removeAll(bayNoRemoves);
        }
    }

    private void analyzeCraneCurCanSelectBay(List<CWPCrane> dpCraneList, CwpData cwpData) {
        for (CWPCrane cwpCrane : dpCraneList) {
            //桥机跨驾驶台作业，当驾驶台一边只剩下一条作业路、或者不影响船期时，则不允许跨驾驶台作业
//            List<Integer> bayNoRemoves = new ArrayList<>();
//            for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
//                CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
//                //倍位所在的那条作业路（安全距离内）已经被其他桥机选择作业了，桥机可以设置为等待状态
//                String craneNo = PublicMethod.safeSpanBayBySelected(cwpBay, cwpData);
//                if (cwpBay.getDpAvailableWorkTime() > 0 && craneNo != null && !craneNo.equals(cwpCrane.getCraneNo())) {
//                    bayNoRemoves.add(bayNo);
//                }
//            }
//            cwpCrane.getDpCurCanSelectBays().removeAll(bayNoRemoves);
            if (cwpCrane.getDpFirstCanSelectBays().size() > 0) {
                CWPBay cwpBayL = cwpData.getCWPBayByBayNo(cwpCrane.getDpFirstCanSelectBays().getFirst());
                if (cwpBayL.getBayNo() % 2 == 0) {
                    CWPBay cwpBayX = getSteppingBay(cwpBayL, cwpData, CWPDomain.L);
                    if (cwpBayX != null && cwpBayX.getDpCurrentTotalWorkTime() > 0) {
                        cwpCrane.getDpCurCanSelectBays().addFirst(cwpBayX.getBayNo());
                    }
                }
                CWPBay cwpBayR = cwpData.getCWPBayByBayNo(cwpCrane.getDpFirstCanSelectBays().getLast());
                if (cwpBayR.getBayNo() % 2 == 0) {
                    CWPBay cwpBayX = getSteppingBay(cwpBayR, cwpData, CWPDomain.R);
                    if (cwpBayX != null && cwpBayX.getDpCurrentTotalWorkTime() > 0) {
                        cwpCrane.getDpCurCanSelectBays().addLast(cwpBayX.getBayNo());
                    }
                }
            }
            //桥机是否需要等待（dpWait = false && canNotWork）：
            //桥机自己范围的作业量做完；
            //旁边桥机必须要作业垫脚（垫脚不做会形成重点路），上次作业的倍位不能继续作业（/又没有其它合适作业的倍位）
            boolean dpWait = false;
        }
    }

    private CWPBay getSteppingBay(CWPBay cwpBay, CwpData cwpData, String side) {
        if (cwpBay.getBayNo() % 2 == 0) {
            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNo1());
            CWPBay cwpBay2 = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNo2());
            if (CWPDomain.L.equals(side)) {
                return cwpBay1.getWorkPosition().compareTo(cwpBay.getWorkPosition()) < 0 ? cwpBay1 : cwpBay2;
            } else if (CWPDomain.R.equals(side)) {
                return cwpBay1.getWorkPosition().compareTo(cwpBay.getWorkPosition()) > 0 ? cwpBay1 : cwpBay2;
            }
        }
        return null;
    }

    private CWPBay removeDividedBay(CWPCrane cwpCrane, CwpData cwpData, String side) {
        CWPBay cwpBay = null;
        if (CWPDomain.L.equals(side)) {
            if (cwpCrane.getDpWorkTimeFrom() < cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                cwpBay = cwpData.getCWPBayByBayNo(cwpCrane.getDpWorkBayNoFrom());
            }
        } else {
            if (cwpCrane.getDpWorkTimeTo() < cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                cwpBay = cwpData.getCWPBayByBayNo(cwpCrane.getDpWorkBayNoTo());
            }
        }
        if (cwpBay != null) {
            return PublicMethod.getSideBay(cwpBay, cwpData, side);
        }
        return null;
    }

    private boolean unnecessaryDividedBay(long wt1, CWPBay cwpBay, CwpData cwpData) {
        long amount = 30;
        long redundancy = amount * cwpData.getWorkingData().getCwpConfig().getOneCntTime();
        if (!CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getCraneSameWorkTime())) {
            return wt1 < redundancy || cwpBay.getDpCurrentTotalWorkTime() - wt1 < redundancy;
        }
        return false;
    }

    private Long getDividedWorkTime(long wt, CWPBay cwpBay, CwpData cwpData) {
        if (cwpBay.getDpAvailableDiscWt() > 0 && Math.abs(cwpBay.getDpAvailableDiscWt() - wt) < 3600) {
            return cwpBay.getDpAvailableDiscWt();
        }
        return null;
    }

    private boolean craneSplitBay(CWPCrane cwpCrane, Integer bayNo, List<CWPCrane> cwpCraneList, CwpData cwpData) {
        if (PublicMethod.getFrontCrane(cwpCrane, cwpCraneList) != null) {
            Long hatchId = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getFirst()).getHatchId();
            if (cwpData.getStructureData().getVMHatchByHatchId(hatchId).getAllBayNos().contains(bayNo)) {
                return true;
            }
            if (cwpData.getStructureData().getRightVMHatch(hatchId) != null) {
                if (cwpData.getStructureData().getRightVMHatch(hatchId).getAllBayNos().contains(bayNo)) {
                    return true;
                }
            }
        }
        if (PublicMethod.getNextCrane(cwpCrane, cwpCraneList) != null) {
            Long hatchId = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getLast()).getHatchId();
            if (cwpData.getStructureData().getVMHatchByHatchId(hatchId).getAllBayNos().contains(bayNo)) {
                return true;
            }
            if (cwpData.getStructureData().getLeftVMHatch(hatchId) != null) {
                if (cwpData.getStructureData().getLeftVMHatch(hatchId).getAllBayNos().contains(bayNo)) {
                    return true;
                }
            }
        }
        return false;
    }
}
