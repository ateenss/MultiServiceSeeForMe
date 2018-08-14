package com.cwp3.data.single;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.crane.CMCraneAddOrDelete;
import com.cwp3.model.crane.CMCraneMaintainPlan;
import com.cwp3.model.crane.CMCranePool;
import com.cwp3.model.crane.CMCraneWorkFlow;
import com.cwp3.model.log.Logger;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMPosition;
import com.cwp3.model.vessel.VMSchedule;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.*;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

public class WorkingData {

    //船期信息
    private Logger logger;
    private VMSchedule vmSchedule;
    private String cwpType;

    private CwpConfig cwpConfig;

    //传入算法的相关数据
    private Map<String, CMCraneWorkFlow> cmCraneWorkFlowMap; //舱作业工艺配置：<hatchId@aboveOrBelow@dlType, CMCraneWorkFlow>
    private Map<String, VMContainer> discContainerMap; //进出口船图箱信息：<vLocation, VMContainer> 小位置，02需要拆成01和03
    private Map<String, VMContainer> loadContainerMap;

    //算法计算生成的数据
    private Map<Long, HatchBlock> hatchBlockMap; //分档信息,key: hatchId
    private Map<String, WorkMove> discWorkMoveMap; //卸船Move信息,key: vLocation 小位置，02需要拆成01和03
    private Map<String, WorkMove> loadWorkMoveMap; //装船Move信息,key

    //多船算法分配的桥机信息、或人工分配船舶桥机池信息
    private List<CMCranePool> cmCranePoolList;
    private List<CMCraneMaintainPlan> cmCraneMaintainPlanList;
    private List<CMCraneAddOrDelete> cmCraneAddOrDeleteList;

    //CWP计划结果
    private Map<String, List<WorkMove>> workMoveMap;
    private Map<String, List<WorkBlock>> workBlockMap;
    private Map<String, List<CraneEfficiency>> craneEfficiencyMap;
    private Map<String, List<AreaTask>> areaTaskMap;

    public WorkingData(VMSchedule vmSchedule) {
        this.logger = new Logger();
        this.vmSchedule = vmSchedule;
        this.cmCraneWorkFlowMap = new HashMap<>();
        this.discContainerMap = new HashMap<>();
        this.loadContainerMap = new HashMap<>();
        this.hatchBlockMap = new HashMap<>();
        this.discWorkMoveMap = new HashMap<>();
        this.loadWorkMoveMap = new HashMap<>();
        cmCranePoolList = new ArrayList<>();
        cmCraneMaintainPlanList = new ArrayList<>();
        cmCraneAddOrDeleteList = new ArrayList<>();
        workMoveMap = new LinkedHashMap<>();
        workBlockMap = new LinkedHashMap<>();
        craneEfficiencyMap = new LinkedHashMap<>();
        areaTaskMap = new LinkedHashMap<>();
    }

    public Map<String, List<WorkMove>> getWorkMoveMap() {
        return workMoveMap;
    }

    public void setWorkMoveMap(Map<String, List<WorkMove>> workMoveMap) {
        this.workMoveMap = workMoveMap;
    }

    public Map<String, List<WorkBlock>> getWorkBlockMap() {
        return workBlockMap;
    }

    public void setWorkBlockMap(Map<String, List<WorkBlock>> workBlockMap) {
        this.workBlockMap = workBlockMap;
    }

    public VMSchedule getVmSchedule() {
        return vmSchedule;
    }

    public Logger getLogger() {
        return logger;
    }

    public CwpConfig getCwpConfig() {
        return cwpConfig;
    }

    public void setCwpConfig(CwpConfig cwpConfig) {
        this.cwpConfig = cwpConfig;
    }

    public String getCwpType() {
        return cwpType;
    }

    public void setCwpType(String cwpType) {
        this.cwpType = cwpType;
    }

    public void addCMCraneWorkFlow(CMCraneWorkFlow cmCraneWorkFlow) {
        cmCraneWorkFlowMap.put(cmCraneWorkFlow.getKey(), cmCraneWorkFlow);
    }

    public CMCraneWorkFlow getCMCraneWorkFlowByKey(String key) {
        return cmCraneWorkFlowMap.get(key);
    }

    public void putVMContainer(VMPosition vmPosition, VMContainer vmContainer) {
        Integer bayNo = vmPosition.getBayNo();
        Integer tierNo = vmPosition.getTierNo();
        Integer rowNo = vmPosition.getRowNo();
        if (bayNo % 2 == 0) { //大倍位
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discContainerMap.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), vmContainer);
                discContainerMap.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), vmContainer);
            }
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                loadContainerMap.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), vmContainer);
                loadContainerMap.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), vmContainer);
            }
        } else {
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discContainerMap.put(vmPosition.getVLocation(), vmContainer);
            }
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                loadContainerMap.put(vmPosition.getVLocation(), vmContainer);
            }
        }
    }

    public VMContainer getVMContainerByVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null && dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            return discContainerMap.get(vmSlot.getVmPosition().getVLocation());
        }
        if (vmSlot != null && dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            return loadContainerMap.get(vmSlot.getVmPosition().getVLocation());
        }
        return null;
    }

    public void addWorkMove(WorkMove workMove) {
        for (VMSlot vmSlot : workMove.getVmSlotSet()) {
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discWorkMoveMap.put(vmSlot.getVmPosition().getVLocation(), workMove);
            }
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                loadWorkMoveMap.put(vmSlot.getVmPosition().getVLocation(), workMove);
            }
        }
    }

    public WorkMove getWorkMoveByVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null) {
            if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
                return discWorkMoveMap.get(vmSlot.getVmPosition().getVLocation());
            }
            if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
                return loadWorkMoveMap.get(vmSlot.getVmPosition().getVLocation());
            }
        }
        return null;
    }

    public Map<String, WorkMove> getDiscWorkMoveMap() {
        return discWorkMoveMap;
    }

    public Map<String, WorkMove> getLoadWorkMoveMap() {
        return loadWorkMoveMap;
    }

    public void addHatchBlock(HatchBlock hatchBlock) {
        this.hatchBlockMap.put(hatchBlock.getHatchId(), hatchBlock);
    }

    public HatchBlock getHatchBlockByHatchId(Long hatchId) {
        return this.hatchBlockMap.get(hatchId);
    }

    /**
     * 船舶左靠，反向靠泊（奇数排靠近海侧）;船舶右靠，正向靠泊（偶数排靠近海侧），得到奇数排号开始，还是偶数排号
     *
     * @param seaOrLand 从海侧开始，还是从陆侧开始
     * @return
     */
    public String getOddOrEvenBySeaOrLand(String seaOrLand) {
        String planBerthDirect = vmSchedule.getPlanBerthDirect();
        boolean sl = seaOrLand.equals(CWPDomain.ROW_SEQ_SEA_LAND);
        String oe = sl ? CWPDomain.ROW_SEQ_ODD_EVEN : CWPDomain.ROW_SEQ_EVEN_ODD;
        String eo = sl ? CWPDomain.ROW_SEQ_EVEN_ODD : CWPDomain.ROW_SEQ_ODD_EVEN;
        return planBerthDirect.equals(CWPDomain.VES_BER_DIRECT_L) ? oe : eo;
    }

    public Integer getHcSeqByWorkMove(Long hatchId, WorkMove workMove) {
        HatchBlock hatchBlock = this.getHatchBlockByHatchId(hatchId);
        String board = workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
        Integer hcSeq = hatchBlock.getHcSeqByRowNo(workMove.getRowNo(), board);
        if (hcSeq == null) {
            hcSeq = hatchBlock.getHcSeqByOtherRowNo(workMove.getRowNo(), board);
        }
        return hcSeq;
    }

    public double getBayPosition(Double bayHatchPo) {
        if (vmSchedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L)) {
            return CalculateUtil.add(vmSchedule.getPlanStartPst(), bayHatchPo);
        } else {
            return CalculateUtil.sub(vmSchedule.getPlanEndPst(), bayHatchPo);
        }
    }

    public void addCMCranePool(CMCranePool cmCranePool) {
        cmCranePoolList.add(cmCranePool);
    }

    public List<CMCranePool> getAllCMCranePools() {
        return cmCranePoolList;
    }

    public List<CMCraneAddOrDelete> getCMCraneAddOrDeleteListByCraneNo(String craneNo) {
        return new ArrayList<>();
    }

    public Map<String, List<CraneEfficiency>> getCraneEfficiencyMap() {
        return craneEfficiencyMap;
    }

    public void setCraneEfficiencyMap(Map<String, List<CraneEfficiency>> craneEfficiencyMap) {
        this.craneEfficiencyMap = craneEfficiencyMap;
    }

    public Map<String, List<AreaTask>> getAreaTaskMap() {
        return areaTaskMap;
    }

    public void setAreaTaskMap(Map<String, List<AreaTask>> areaTaskMap) {
        this.areaTaskMap = areaTaskMap;
    }
}
