package com.cwp3.single.algorithm.move.method;

import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.crane.CMCraneWorkFlow;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.single.algorithm.move.maker.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by csw on 2017/9/21.
 * Description: 作业工艺、作业顺序相关工具方法类
 */
public class PublicMethod {

    public static List<AbstractMaker> getPTSeqListByCMCraneWorkFlow(CMCraneWorkFlow cmCraneWorkFlow) {
        List<AbstractMaker> ptSeqList = new ArrayList<>();
        if (cmCraneWorkFlow != null) {
            if (cmCraneWorkFlow.getSingle()) {
                ptSeqList.add(new M20Single());
                ptSeqList.add(new M40Single());
            }
            if (cmCraneWorkFlow.getTwin()) {
                ptSeqList.add(new M20Dual());
            }
            if (cmCraneWorkFlow.getTandem()) {
                ptSeqList.add(new M40Dual());
            }
        } else {
            ptSeqList.add(new M20Single());
            ptSeqList.add(new M40Single());
            ptSeqList.add(new M20Dual());
        }
        return ptSeqList;
    }

    public static List<AbstractMaker> getPTSeqListByBayNoAndDlType(Integer bayNo, String dlType) {
        List<AbstractMaker> ptSeqList = new ArrayList<>();
        if (bayNo % 2 == 0) {
            if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
                ptSeqList.add(new M40Single());
                ptSeqList.add(new M20Dual());
            } else {
                ptSeqList.add(new M20Dual());
                ptSeqList.add(new M40Single());
            }
        } else {
            ptSeqList.add(new M20Single());
        }
        return ptSeqList;
    }

    public static boolean hasNoneWorkFlow(String workflow) {
        return !Arrays.asList(CWPCraneDomain.CT_SINGLE20, CWPCraneDomain.CT_DUAL20, CWPCraneDomain.CT_SINGLE40, CWPCraneDomain.CT_DUAL40).contains(workflow);
    }

    public static boolean isSingleWorkFlow(String workFlow) {
        return CWPCraneDomain.CT_SINGLE20.equals(workFlow) || CWPCraneDomain.CT_SINGLE40.equals(workFlow);
    }

    public static long getCntWorkTime(VMContainer vmContainer, CwpConfig cwpConfig) {
        if (vmContainer.getCntWorkTime() != null && vmContainer.getWorkFlow() != null) {
            switch (vmContainer.getWorkFlow()) {
                case CWPCraneDomain.CT_SINGLE20:
                    return vmContainer.getCntWorkTime() > cwpConfig.getSingle20Time() ? vmContainer.getCntWorkTime() : cwpConfig.getSingle20Time();
                case CWPCraneDomain.CT_SINGLE40:
                    if ("40".equals(vmContainer.getSize())) {
                        return vmContainer.getCntWorkTime() > cwpConfig.getSingle40Time() ? vmContainer.getCntWorkTime() : cwpConfig.getSingle40Time();
                    } else if ("45".equals(vmContainer.getSize())) {
                        return vmContainer.getCntWorkTime() > cwpConfig.getSingle45Time() ? vmContainer.getCntWorkTime() : cwpConfig.getSingle45Time();
                    } else {
                        return vmContainer.getCntWorkTime() > cwpConfig.getSingle40Time() ? vmContainer.getCntWorkTime() : cwpConfig.getSingle40Time();
                    }
                case CWPCraneDomain.CT_DUAL20:
                    return vmContainer.getCntWorkTime() > cwpConfig.getDouble20Time() ? vmContainer.getCntWorkTime() : cwpConfig.getDouble20Time();
                case CWPCraneDomain.CT_DUAL40:
                    if ("40".equals(vmContainer.getSize())) {
                        return vmContainer.getCntWorkTime() > cwpConfig.getDouble40Time() ? vmContainer.getCntWorkTime() : cwpConfig.getDouble40Time();
                    } else if ("45".equals(vmContainer.getSize())) {
                        return vmContainer.getCntWorkTime() > cwpConfig.getDouble45Time() ? vmContainer.getCntWorkTime() : cwpConfig.getDouble45Time();
                    } else {
                        return vmContainer.getCntWorkTime() > cwpConfig.getDouble40Time() ? vmContainer.getCntWorkTime() : cwpConfig.getDouble40Time();
                    }
                default:
                    return vmContainer.getCntWorkTime();
            }
        }
        return cwpConfig.getOneCntTime();
    }

    public static String getWorkFlowStr(String workFlow) {
        switch (workFlow) {
            case CWPCraneDomain.CT_SINGLE20:
                return "1";
            case CWPCraneDomain.CT_SINGLE40:
                return "1";
            case CWPCraneDomain.CT_DUAL20:
                return "2";
            case CWPCraneDomain.CT_DUAL40:
                return "3";
            default:
                return "";
        }
    }
}
