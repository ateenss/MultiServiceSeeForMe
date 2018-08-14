package com.cwp3.single.algorithm.cwp.method;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.modal.CWPBay;
import com.cwp3.single.algorithm.cwp.modal.CWPCrane;
import com.cwp3.single.algorithm.cwp.modal.DPPair;
import com.cwp3.single.algorithm.cwp.modal.DPResult;
import com.cwp3.single.data.CwpData;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

/**
 * Created by csw on 2017/9/19.
 * Description:
 */
public class PublicMethod {

    public static void sortCWPBayByWorkPosition(List<CWPBay> cwpBayList) {
        Collections.sort(cwpBayList, new Comparator<CWPBay>() {
            @Override
            public int compare(CWPBay o1, CWPBay o2) {
                return o1.getWorkPosition().compareTo(o2.getWorkPosition());
            }
        });
    }

    public static void sortCWPCraneByCraneSeq(List<CWPCrane> cwpCraneList) {
        Collections.sort(cwpCraneList, new Comparator<CWPCrane>() {
            @Override
            public int compare(CWPCrane o1, CWPCrane o2) {
                if (o1.getCraneSeq() != null && o2.getCraneSeq() != null) {
                    return o1.getCraneSeq().compareTo(o2.getCraneSeq());
                } else {
                    if (o1.getDpCurrentWorkPosition().equals(o2.getDpCurrentWorkPosition())) {
                        return o1.getCraneNo().compareTo(o2.getCraneNo());
                    } else {
                        return o1.getDpCurrentWorkPosition().compareTo(o2.getDpCurrentWorkPosition());
                    }
                }
            }
        });
    }

    public static void sortWorkMoveListByPlanStartTime(List<WorkMove> workMoveList) {
        Collections.sort(workMoveList, new Comparator<WorkMove>() {
            @Override
            public int compare(WorkMove o1, WorkMove o2) {
                return o1.getPlanStartTime().compareTo(o2.getPlanStartTime());
            }
        });
    }

    public static void sortWorkMoveListByMoveOrder(List<WorkMove> workMoveList) {
        Collections.sort(workMoveList, new Comparator<WorkMove>() {
            @Override
            public int compare(WorkMove o1, WorkMove o2) {
                return o1.getMoveOrder().compareTo(o2.getMoveOrder());
            }
        });
    }

    public static long getCurTotalWorkTime(List<CWPBay> cwpBays) {
        long wt = 0;
        for (CWPBay cwpBay : cwpBays) {
            wt += cwpBay.getDpCurrentTotalWorkTime();
        }
        return wt;
    }


    public static int getMaxCraneNum(List<CWPBay> cwpBays, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        int maxCraneNum = 0;
        for (int j = 0; j < cwpBays.size(); ) {
            CWPBay cwpBayJ = cwpBays.get(j);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                int k = j;
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance >= craneSafeSpan) {
                        break;
                    }
                }
                j = k;
                maxCraneNum++;
            } else {
                j++;
            }
        }
        return maxCraneNum;
    }

    public static Integer getSelectBayNoInDpResult(String craneNo, DPResult dpResult) {
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            String craneNo1 = (String) dpPair.getFirst();
            if (craneNo1.equals(craneNo)) {
                return (Integer) dpPair.getSecond();
            }
        }
        return null;
    }

    public static Integer getCurBayNoInCranePosition(String craneNo, List<DPPair> dpCranePosition) {
        for (DPPair dpPair : dpCranePosition) {
            String craneNo1 = (String) dpPair.getFirst();
            if (craneNo1.equals(craneNo)) {
                return (Integer) dpPair.getSecond();
            }
        }
        return null;
    }


    public static String safeSpanBayBySelected(CWPBay cwpBay, CwpData cwpData) {
        List<DPPair> dpTraceBack = cwpData.getDpResult().getDpTraceBack();
        for (DPPair dpPair : dpTraceBack) {
            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            if (safeSpanBay(cwpBay, cwpBay1, cwpData.getWorkingData().getCwpConfig().getSafeDistance())) {
                return (String) dpPair.getFirst();
            }
        }
        return null;
    }

    private static boolean safeSpanBay(CWPBay cwpBay, CWPBay cwpBay1, Double safeSpan) {
        return Math.abs(CalculateUtil.sub(cwpBay.getWorkPosition(), cwpBay1.getWorkPosition())) < 2 * safeSpan;
    }

    public static boolean safeSpanBay(Integer bayNo, Integer bayNo1, CwpData cwpData) {
        return safeSpanBay(cwpData.getCWPBayByBayNo(bayNo), cwpData.getCWPBayByBayNo(bayNo1), cwpData.getWorkingData().getCwpConfig().getSafeDistance());
    }

    public static boolean selectBaysIncludeMachine(CWPCrane cwpCrane, CwpData cwpData) {
        boolean include = false;
        if (cwpCrane.getDpCurCanSelectBays().size() > 0) {
            CWPBay cwpBayL = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getFirst());
            CWPBay cwpBayR = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getLast());
            for (CWPBay cwpMachine : cwpData.getAllMachineBays()) {
                if (cwpMachine.getWorkPosition() > cwpBayL.getWorkPosition() && cwpMachine.getWorkPosition() < cwpBayR.getWorkPosition()) {
                    include = true;
                }
            }
        }
        return include;
    }

    public static long getCraneCurAllWorkTime(CWPCrane cwpCrane, CwpData cwpData) {
        long wt = 0;
        for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
            wt += cwpData.getCWPBayByBayNo(bayNo).getDpCurrentTotalWorkTime();
        }
        return wt;
    }

    public static CWPBay getNextBay(CWPBay cwpBay, CwpData cwpData) {
        List<CWPBay> cwpBayList = cwpData.getAllCWPBays();
        for (int j = 0; j < cwpBayList.size(); j++) {
            if (cwpBayList.get(j).getBayNo().equals(cwpBay.getBayNo())) {
                if (j + 1 < cwpBayList.size()) {
                    return cwpBayList.get(j + 1);
                }
            }
        }
        return cwpBay;
    }

    public static CWPBay getFrontBay(CWPBay cwpBay, CwpData cwpData) {
        List<CWPBay> cwpBayList = cwpData.getAllCWPBays();
        for (int j = 0; j < cwpBayList.size(); j++) {
            if (cwpBayList.get(j).getBayNo().equals(cwpBay.getBayNo())) {
                if (j - 1 >= 0) {
                    return cwpBayList.get(j - 1);
                }
            }
        }
        return cwpBay;
    }

    public static CWPBay getSideBay(CWPBay cwpBay, CwpData cwpData, String side) {
        if (CWPDomain.L.equals(side)) {
            return getNextBay(cwpBay, cwpData);
        } else {
            return getFrontBay(cwpBay, cwpData);
        }
    }

    public static Map<Integer, List<CWPBay>> getCurEveryRoadBayMap(List<CWPBay> cwpBays, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        Map<Integer, List<CWPBay>> everyRoadBayMap = new LinkedHashMap<>();
        for (int j = 0; j < cwpBays.size(); ) {
            CWPBay cwpBayJ = cwpBays.get(j);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                int k = j;
                everyRoadBayMap.put(cwpBayJ.getBayNo(), new ArrayList<CWPBay>());
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance >= craneSafeSpan) {
                        break;
                    }
                    if (cwpBayK.getDpCurrentTotalWorkTime() > 0) {
                        everyRoadBayMap.get(cwpBayJ.getBayNo()).add(cwpBayK);
                    }
                }
                j = k;
            } else {
                j++;
            }
        }
        return everyRoadBayMap;
    }

    public static Boolean craneThroughMachine(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        boolean throughMachine = false;
        for (CWPBay cwpBay1 : cwpData.getAllMachineBays()) {
            double machinePo = cwpBay1.getWorkPosition();
            if ((machinePo > cwpBay.getWorkPosition() && machinePo < cwpCrane.getDpCurrentWorkPosition())
                    || (machinePo > cwpCrane.getDpCurrentWorkPosition() && machinePo < cwpBay.getWorkPosition())) {
                throughMachine = true;
                break;
            }
        }
        return throughMachine;
    }

    public static List<CWPCrane> copyCwpCraneList(List<CWPCrane> dpCraneList) {
        List<CWPCrane> cwpCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane : dpCraneList) {
            cwpCraneList.add(cwpCrane.deepCopy());
        }
        return cwpCraneList;
    }

    public static List<CWPBay> getMaxWorkTimeCWPBayList(List<CWPBay> cwpBays, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        long maxWorkTime = Long.MIN_VALUE;
        List<CWPBay> maxCwpBayList = new ArrayList<>();
        for (int j = 0; j < cwpBays.size(); j++) {
            CWPBay cwpBayJ = cwpBays.get(j);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                int k = j;
                Long tempWorkTime = 0L;
                List<CWPBay> tempCwpBayList = new ArrayList<>();
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance < craneSafeSpan) {
                        if (cwpBayK.getDpCurrentTotalWorkTime() > 0) {
                            tempWorkTime += cwpBayK.getDpCurrentTotalWorkTime();
                            tempCwpBayList.add(cwpBayK);
                        }
                    } else {
                        if (tempWorkTime > maxWorkTime) {
                            maxWorkTime = tempWorkTime;
                            maxCwpBayList.clear();
                            maxCwpBayList.addAll(tempCwpBayList);
                        }
                        break;
                    }
                }
            }
        }
        return maxCwpBayList;
    }

    public static List<CWPBay> getSideCwpBayList(String side, List<CWPBay> maxCwpBayList, List<CWPBay> cwpBays) {
        List<CWPBay> sideCwpBayList = new ArrayList<>();
        if (maxCwpBayList.isEmpty() || cwpBays.isEmpty()) {
            return sideCwpBayList;
        }
        sortCwpBayByWorkPosition(maxCwpBayList);
        for (CWPBay cwpBay : cwpBays) {
            if (side.equals(CWPDomain.L)) {
                if (cwpBay.getWorkPosition().compareTo(maxCwpBayList.get(0).getWorkPosition()) < 0) {
                    sideCwpBayList.add(cwpBay);
                }
            }
            if (side.equals(CWPDomain.R)) {
                if (cwpBay.getWorkPosition().compareTo(maxCwpBayList.get(maxCwpBayList.size() - 1).getWorkPosition()) > 0) {
                    sideCwpBayList.add(cwpBay);
                }
            }
        }
        return sideCwpBayList;
    }

    private static void sortCwpBayByWorkPosition(List<CWPBay> cwpBayList) {
        Collections.sort(cwpBayList, new Comparator<CWPBay>() {
            @Override
            public int compare(CWPBay o1, CWPBay o2) {
                return o1.getWorkPosition().compareTo(o2.getWorkPosition());
            }
        });
    }

    public static CWPCrane getFrontCrane(CWPCrane cwpCrane, List<CWPCrane> cwpCraneList) {
        for (int i = 0; i < cwpCraneList.size(); i++) {
            if (cwpCraneList.get(i).getCraneNo().equals(cwpCrane.getCraneNo())) {
                if (i - 1 >= 0) {
                    return cwpCraneList.get(i - 1);
                }
            }
        }
        return null;
    }

    public static CWPCrane getNextCrane(CWPCrane cwpCrane, List<CWPCrane> cwpCraneList) {
        for (int i = 0; i < cwpCraneList.size(); i++) {
            if (cwpCraneList.get(i).getCraneNo().equals(cwpCrane.getCraneNo())) {
                if (i + 1 < cwpCraneList.size()) {
                    return cwpCraneList.get(i + 1);
                }
            }
        }
        return null;
    }

    public static List<DPPair> copyDpPairList(List<DPPair> dpPairList) {
        List<DPPair> dpPairList1 = new ArrayList<>();
        for (DPPair dpPair : dpPairList) {
            DPPair dpPair1 = new DPPair<>(dpPair.getFirst(), dpPair.getSecond());
            dpPairList1.add(dpPair1);
        }
        return dpPairList1;
    }

    public static CWPCrane getCwpCraneByNo(String craneNo, List<CWPCrane> cwpCranes) {
        for (CWPCrane cwpCrane : cwpCranes) {
            if (cwpCrane.getCraneNo().equals(craneNo)) {
                return cwpCrane;
            }
        }
        return null;
    }

    public static boolean inDpPairList(DPPair dpPair, List<DPPair> dpPairList) {
        for (DPPair dpPair1 : dpPairList) {
            if (dpPair1.equals(dpPair)) {
                return true;
            }
        }
        return false;
    }

    public static CWPCrane getCwpCraneByCraneNo(String craneNo, List<CWPCrane> dpCwpCraneList) {
        for (CWPCrane cwpCrane : dpCwpCraneList) {
            if (cwpCrane.getCraneNo().equals(craneNo)) {
                return cwpCrane;
            }
        }
        return null;
    }

    public static boolean isDividedBay(CWPCrane cwpCrane, CWPBay cwpBay, List<CWPCrane> cwpCraneList) {
        if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoFrom())) {
            CWPCrane frontCrane = getFrontCrane(cwpCrane, cwpCraneList);
            if (frontCrane != null && cwpBay.getBayNo().equals(frontCrane.getDpWorkBayNoTo()) && frontCrane.getDpWorkTimeTo() > 144) {
                return true;
            }
        }
        if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoTo())) {
            CWPCrane nextCrane = getNextCrane(cwpCrane, cwpCraneList);
            if (nextCrane != null && cwpBay.getBayNo().equals(nextCrane.getDpWorkBayNoFrom()) && nextCrane.getDpWorkTimeFrom() > 144) {
                return true;
            }
        }
        return false;
    }
}
