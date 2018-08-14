package com.cwp3.single.algorithm.cwp.decision;

import com.cwp3.model.vessel.VMHatch;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;

import java.util.*;

/**
 * Created by csw on 2018/6/13.
 * Description:
 */
public class Evaluator {

    public List<DPBranch> getFirstDpBranchList1(CwpData cwpData) {
        List<DPBranch> dpBranchList = new ArrayList<>();
        List<CWPBay> cwpBays = cwpData.getAllCWPBays();
        for (List<CWPCrane> cwpCranes : cwpData.getDpFirstCwpCraneList()) {

            int n = 1;
            for (CWPCrane cwpCrane : cwpCranes) {
                n *= cwpCrane.getDpSelectBays().size() > 0 ? cwpCrane.getDpSelectBays().size() : 1;
            }
            cwpData.getWorkingData().getLogger().logDebug("所有分支：" + n);

            List<List<DPPair>> dpPairLists = new ArrayList<>();
            CWPCrane cwpCrane1 = getFirstUsableCrane(cwpCranes);
            if (cwpCrane1 != null) {
                for (Integer bayNo : cwpCrane1.getDpSelectBays()) {
                    List<DPPair> dpPairList1 = new ArrayList<>();
                    DPPair dpPair1 = new DPPair<>(cwpCrane1.getCraneNo(), bayNo);
                    dpPairList1.add(dpPair1);
                    dpPairLists.add(dpPairList1);
                }
                for (int i = 1; i < cwpCranes.size(); i++) {
                    CWPCrane cwpCrane = cwpCranes.get(i);
                    if (cwpCrane.getFirstWorkBayNo() == null) {
                        List<List<DPPair>> tempDpPairLists = new ArrayList<>();
                        for (List<DPPair> dpPairList : dpPairLists) {
                            for (Integer bayNo : cwpCrane.getDpSelectBays()) {
                                Integer bayNo1 = (Integer) dpPairList.get(dpPairList.size() - 1).getSecond();
                                if (!PublicMethod.safeSpanBay(bayNo, bayNo1, cwpData)) {
                                    List<DPPair> tempDpPairList = PublicMethod.copyDpPairList(dpPairList);
                                    DPPair dpPair = new DPPair<>(cwpCrane.getCraneNo(), bayNo);
                                    tempDpPairList.add(dpPair);
                                    tempDpPairLists.add(tempDpPairList);
                                }
                            }
                        }
                        if (tempDpPairLists.size() > 0) {
                            dpPairLists = tempDpPairLists;
                        }
                    }
                }
            }
            cwpData.getWorkingData().getLogger().logDebug("缩减后分支：" + dpPairLists.size());

            List<DPCraneSelectBay> dpCraneSelectBayList = new ArrayList<>();
            for (CWPCrane cwpCrane : cwpCranes) {
                for (CWPBay cwpBay : cwpBays) {
                    DPCraneSelectBay dpCraneSelectBay = new DPCraneSelectBay(new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo()));
                    dpCraneSelectBay.setDpDistance(Math.abs(cwpCrane.getDpCurrentWorkPosition() - cwpBay.getWorkPosition()));
                    dpCraneSelectBay.setDpWorkTime(cwpBay.getDpAvailableWorkTime());
                    if (cwpBay.getDpAvailableWorkTime() > 0) {
                        if (cwpCrane.getFirstWorkBayNo() != null) {
                            if (cwpBay.getBayNo().equals(cwpCrane.getFirstWorkBayNo())) {
                                dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.inWorkRange.getCode(), CWPDesc.inWorkRange.getDesc()));
                            } else {
                                dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.outWorkRange.getCode(), CWPDesc.outWorkRange.getDesc()));
                            }
                        } else {
                            if (cwpCrane.getDpCurCanSelectBays().contains(cwpBay.getBayNo())) {
                                dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.inWorkRange.getCode(), CWPDesc.inWorkRange.getDesc()));
                            } else {
                                dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.outWorkRange.getCode(), CWPDesc.outWorkRange.getDesc()));
                            }
                        }
                    } else {
                        dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.canNotWork.getCode(), CWPDesc.canNotWork.getDesc()));
                    }
                    dpCraneSelectBayList.add(dpCraneSelectBay);
                }
            }

            DPBranch defaultDpBranch = new DPBranch();
            defaultDpBranch.setDpCwpCraneList(cwpCranes);
            defaultDpBranch.getDpCraneSelectBays().addAll(dpCraneSelectBayList);
            dpBranchList.add(defaultDpBranch);
            for (List<DPPair> dpPairList : dpPairLists) {
                DPBranch dpBranch = new DPBranch();
                for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
                    if (PublicMethod.inDpPairList(dpCraneSelectBay.getDpPair(), dpPairList)) {
                        DPCraneSelectBay dpCraneSelectBay1 = new DPCraneSelectBay(dpCraneSelectBay.getDpPair());
                        dpCraneSelectBay1.setDpDistance(dpCraneSelectBay.getDpDistance());
                        dpCraneSelectBay1.setDpWorkTime(dpCraneSelectBay.getDpWorkTime());
                        dpCraneSelectBay1.setDpFeature(new DPFeature(CWPDesc.firstSelectFactor.getCode(), CWPDesc.firstSelectFactor.getDesc()));
                        dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay1);
                    } else {
                        dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay);
                    }
                }
                dpBranch.setDpCwpCraneList(cwpCranes);
                dpBranchList.add(dpBranch);
            }
        }
        cwpData.getWorkingData().getLogger().logInfo("Branch number：" + dpBranchList.size());
        return dpBranchList;
    }

    public DPBranch getCurDpBranch(CwpData cwpData) {
        List<CWPCrane> cwpCranes = cwpData.getDpCwpCraneList();
        List<CWPBay> cwpBays = cwpData.getAllCWPBays();
        List<DPCraneSelectBay> dpCraneSelectBayList = new ArrayList<>();
        for (CWPCrane cwpCrane : cwpCranes) {
            boolean dpWait = true;
            for (CWPBay cwpBay : cwpBays) {
                DPCraneSelectBay dpCraneSelectBay = new DPCraneSelectBay(new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo()));
                dpCraneSelectBay.setDpDistance(Math.abs(cwpCrane.getDpCurrentWorkPosition() - cwpBay.getWorkPosition()));
                dpCraneSelectBay.setDpWorkTime(cwpBay.getDpAvailableWorkTime());
                dpCraneSelectBay.setTroughMachine(PublicMethod.craneThroughMachine(cwpCrane, cwpBay, cwpData));
                if (cwpBay.getDpAvailableWorkTime() > 0 && !cwpCrane.getDpWait()) {
                    if (craneCanSelectBay(cwpCrane, cwpBay, cwpData)) {
                        dpWait = false;
                        DPFeature dpFeature = new DPFeature(CWPDesc.inWorkRange.getCode(), CWPDesc.inWorkRange.getDesc());
                        if (lastSelectHatch(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.lastSelectBay.getCode(), CWPDesc.lastSelectBay.getDesc());
                        }
                        if (steppingCntFirst(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.steppingCntFirst.getCode(), CWPDesc.steppingCntFirst.getDesc());

                        }
                        dpCraneSelectBay.getDpFeatureList().add(dpFeature);
                    } else {
                        dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.outWorkRange.getCode(), CWPDesc.outWorkRange.getDesc()));
                    }
                } else {
                    dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.canNotWork.getCode(), CWPDesc.canNotWork.getDesc()));
                }
                dpCraneSelectBayList.add(dpCraneSelectBay);
            }
            cwpCrane.setDpWait(dpWait);
        }
        DPBranch dpBranch = new DPBranch();
        for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
            for (DPFeature dpFeature : dpCraneSelectBay.getDpFeatureList()) {
                dpCraneSelectBay.setDpFeature(dpFeature);
                dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay);
            }
        }
        return dpBranch;
    }

    private boolean steppingCntFirst(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        //注意垫脚箱的判断，有些是单边箱应该也选择优先作业
        if (cwpBay.getDpSteppingCntFlag()) {
            if (cwpData.getDpResult().getDpTraceBack().size() > 0) {
                Integer bayNo = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
                if (bayNo == null) { //桥机上次未选择倍位作业，则看桥机当前所停的位置
                    bayNo = PublicMethod.getCurBayNoInCranePosition(cwpCrane.getCraneNo(), cwpData.getDpResult().getDpCranePosition());
                }
                if (bayNo != null) {
                    CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNo);
                    return cwpBay.getHatchId().equals(cwpBayLast.getHatchId());
                }
            }
        }
        return false;
    }

    private boolean craneCanSelectBay(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        boolean canSelect = false;
        //1.1：桥机平均作业量范围内的倍位
        if (cwpCrane.getDpCurCanSelectBays().contains(cwpBay.getBayNo())) {
            canSelect = true;
        }
        //1.2：桥机上次选择的是这个舱作业
//        if (lastSelectHatch(cwpCrane, cwpBay, cwpData)) {
//            canSelect = true;
//        }
        if (canSelect) { //特殊情况桥机不能选择倍位作业
            //2.1：桥机过烟囱/驾驶台作业，一般不允许跨驾驶台作业 只剩下一条作业路、或者不影响船期，则不允许跨驾驶台作业
//            if (selectBaysIncludeMachine(cwpCrane, cwpBay, cwpData)) {
//                canSelect = false;
//            }
        }
        return canSelect;
    }

    private boolean craneThroughMachine(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        boolean throughMachine = false;
        for (CWPBay cwpMachine : cwpData.getAllMachineBays()) {
            double machinePo = cwpMachine.getWorkPosition();
            if ((machinePo > cwpBay.getWorkPosition() && machinePo < cwpCrane.getDpCurrentWorkPosition())
                    || (machinePo > cwpCrane.getDpCurrentWorkPosition() && machinePo < cwpBay.getWorkPosition())) {
                throughMachine = true;
            }
        }
        return throughMachine;
    }

    private boolean workTimeFactor(CWPBay cwpBay, CwpData cwpData) {
        return true;
    }

    private boolean splitRoad(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        VMHatch frontHatch = cwpData.getStructureData().getLeftVMHatch(cwpBay.getHatchId());
        VMHatch nextHatch = cwpData.getStructureData().getRightVMHatch(cwpBay.getHatchId());
        if (frontHatch != null && nextHatch != null) {
            CWPBay frontBay = cwpData.getCWPBayByBayNo(frontHatch.getBayNoD());
            CWPBay nextBay = cwpData.getCWPBayByBayNo(nextHatch.getBayNoD());
            return frontBay.getDpAvailableWorkTime() > 0 && nextBay.getDpAvailableWorkTime() > 0;
        }
        return false;
    }

    private boolean lastSelectHatch(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        if (cwpData.getDpResult().getDpTraceBack().size() > 0) {
            Integer bayNo = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
            if (bayNo == null) { //桥机上次未选择倍位作业，则看桥机当前所在的位置
                bayNo = PublicMethod.getCurBayNoInCranePosition(cwpCrane.getCraneNo(), cwpData.getDpResult().getDpCranePosition());
            }
            if (bayNo != null) {
                CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNo);
                if (cwpBayLast.getDpAvailableWorkTime() > 0) {
                    return cwpBay.getBayNo().equals(cwpBayLast.getBayNo());
                } else {
                    return cwpBay.getHatchId().equals(cwpBayLast.getHatchId());
                }
            }
        }
        return false;
    }

    private CWPCrane getFirstUsableCrane(List<CWPCrane> cwpCranes) {
        for (CWPCrane cwpCrane : cwpCranes) {
            if (cwpCrane.getFirstWorkBayNo() == null && cwpCrane.getDpSelectBays().size() > 0) {
                return cwpCrane;
            }
        }
        return null;
    }

    public boolean invalidBranch(CwpData cwpData) {
        long vesselTime = cwpData.getVesselTime();
        Map<Integer, List<CWPBay>> everyRoadMap = PublicMethod.getCurEveryRoadBayMap(cwpData.getAllCWPBays(), cwpData);
        for (List<CWPBay> cwpBayList : everyRoadMap.values()) {
            long roadWt = PublicMethod.getCurTotalWorkTime(cwpBayList);
            if (roadWt > vesselTime) {
                cwpData.getWorkingData().getLogger().logDebug("......去掉不能满足船期的分支......");
                return true;
            }
        }
        return false;
    }

    public List<CwpData> getBestResult(List<CwpData> cwpDataList) {
        List<CwpData> resultList = new ArrayList<>();
        //在满足条件的结果中，选择最优的结果
        Collections.sort(cwpDataList, new Comparator<CwpData>() {
            @Override
            public int compare(CwpData o1, CwpData o2) {
                if (o1.getDpMoveNumber().equals(o2.getDpMoveNumber())) {
                    return o1.getDpCurrentTime().compareTo(o2.getDpCurrentTime());
                } else {
                    return o1.getDpMoveNumber().compareTo(o2.getDpMoveNumber());
                }
            }
        });
        //移动次数最少->移动距离最少->作业时间最短
        //分割倍位优先作业
        //装卸平衡参数
        resultList.add(cwpDataList.get(0));
        return resultList;
    }
}
